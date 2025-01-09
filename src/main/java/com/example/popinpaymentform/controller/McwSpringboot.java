package com.example.embeddedpaymentform.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.HashMap;
import java.util.Map;

@Controller
public class McwSpringboot {
    
    @Autowired
    private McwProperties properties;

    @Autowired
    private McwController mcwController;
    

    /**
     * @@ Manejo de solicitudes GET para la ruta raíz, checkout y result @@
     */
    @GetMapping({"/", "/checkout", "/result"})
    public String handleGet(Model model) {
        // Generate orderId
        String orderId = mcwController.generateOrderId();
	// Agrega el oderID al model para ser usado en el template
        model.addAttribute("orderId", orderId);
	// Renderiza el template
        return "index";
    }
    
    /**
     * @@ Manejo de solicitudes POST para checkout @@
     */
    @PostMapping("/checkout")
    public String handleCheckout(
            @RequestParam Map<String, String> allParams, 
            Model model) {
        
        // Creación de un MAP para almacenar los parámetros
        Map<String, String> parameters = new HashMap<>();
        String[] requiredParams = {"firstName", "lastName", "email", "phoneNumber", "identityType", 
                                   "identityCode", "address", "country", "state", "city", "zipCode", 
                                   "orderId", "amount", "currency"};
        
	// Extrae los parámetros requeridos para la generación del FormToken
        for (String param : requiredParams) {
            parameters.put(param, allParams.get(param));
        }

        // Obtener PublicKey
        String PUBLIC_KEY = properties.getProperty("PUBLIC_KEY");

        // Obtenemos el FormToken generado
        String formToken = mcwController.generateFormToken(parameters);
        
	// Agrega el FormToken y el PublicKey al modelo
        model.addAttribute("formToken", formToken);
        model.addAttribute("PUBLIC_KEY", PUBLIC_KEY);
        
	// Renderiza el template
        return "checkout";
    }
    
    /**
     * @@ Manejo de solicitudes POST para result @@
     */
    @PostMapping("/result")
        public String processResult(
	@RequestParam Map<String, String> resultParameters,
        Model model
    	) {
	
	String HMAC_SHA256 = properties.getProperty("HMAC_SHA256");
	
	// Asignando los valores de la respuesta de Izipay en las variables
	String krHash = resultParameters.get("kr-hash");
        String krHashAlgorithm = resultParameters.get("kr-hash-algorithm");
        String krAnswerType = resultParameters.get("kr-answer-type");
        String krAnswer = resultParameters.get("kr-answer");
        String krHashKey = resultParameters.get("kr-hash-key");

	// Válida que la respuesta sea íntegra comprando el hash recibido en el 'kr-hash' con el generado con el 'kr-answer'
	if (!mcwController.checkHash(krHash, HMAC_SHA256, krAnswer)){
		return "error";
	}

        // Almacenamos los datos del kr-answer en Json
        JSONObject jsonResponse = new JSONObject(krAnswer);
	// Convertimos el valor del JSON obtenido a 'pretty print' para su visualización en el template
        String pJson = jsonResponse.toString(4);
        
	// Almacenamos los datos del pago en las variables
        String orderStatus = jsonResponse.getString("orderStatus");
        int orderTotalAmount = jsonResponse.getJSONObject("orderDetails").getInt("orderTotalAmount");
        String orderId = jsonResponse.getJSONObject("orderDetails").getString("orderId");
        String currency = jsonResponse.getJSONObject("orderDetails").getString("orderCurrency");

        // Formatear el valor de 'amount' de centimos a decimales
        double orderAmountDouble = (double) orderTotalAmount / 100;
        String orderAmount = String.format("%.02f", orderAmountDouble);

	// Agrega diferentes atributos al modelo
        model.addAttribute("krHash", krHash);
        model.addAttribute("krHashAlgorithm", krHashAlgorithm);
        model.addAttribute("krAnswerType", krAnswerType);
        model.addAttribute("krAnswer", krAnswer);
        model.addAttribute("krHashKey", krHashKey);
        model.addAttribute("pJson", pJson);
        model.addAttribute("orderStatus", orderStatus);
        model.addAttribute("orderTotalAmount", orderAmount);
        model.addAttribute("orderId", orderId);
        model.addAttribute("currency", currency);
            
	// Renderiza el template
        return "result";
        
    }

    /**
     * @@ Manejo de solicitudes POST para IPN @@
     */
    @PostMapping("/ipn")
    @ResponseBody
	public String processIpn(
		@RequestParam Map<String, String> ipnParameters
    	) {
	
	String PASSWORD = properties.getProperty("PASSWORD");
		
	// Asignando los valores de la respuesta IPN en las variables
	String krHash = ipnParameters.get("kr-hash");
        String krAnswer = ipnParameters.get("kr-answer");
        
	// Válida que la respuesta sea íntegra comprando el hash recibido en el 'kr-hash' con el generado con el 'kr-answer'
	if (!mcwController.checkHash(krHash, PASSWORD, krAnswer)){
		return "No valid IPN";
	}

	// Procesa la respuesta del pago en Json
        JSONObject jsonResponse = new JSONObject(krAnswer);
        
	// Ejemplo de extracción de datos
        JSONArray transactionsArray = jsonResponse.getJSONArray("transactions");
        JSONObject transactions = transactionsArray.getJSONObject(0);
        
	// Verifica el orderStatus PAID
        String orderStatus = jsonResponse.getString("orderStatus");
        String orderId = jsonResponse.getJSONObject("orderDetails").getString("orderId");
        String uuid = transactions.getString("uuid");
        
	// Retornando el Order Status
        return "OK! Order Status: " + orderStatus;
    }
}
