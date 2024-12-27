<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/logos_izipay/logo-izipay-banner-1140x100.png?raw=true" alt="Formulario" width=100%/>
</p>

# Popin-PaymentForm-Springboot-Java

## Índice

➡️ [1. Introducción](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#%EF%B8%8F-1-introducci%C3%B3n)  
🔑 [2. Requisitos previos](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#-2-requisitos-previos)  
🚀 [3. Ejecutar ejemplo](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#-3-ejecutar-ejemplo)  
🔗 [4. Pasos de integración](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#4-pasos-de-integraci%C3%B3n)  
💻 [4.1. Desplegar pasarela](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#41-desplegar-pasarela)  
💳 [4.2. Analizar resultado de pago](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#42-analizar-resultado-del-pago)  
📡 [4.3. Pase a producción](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#43pase-a-producci%C3%B3n)  
🎨 [5. Personalización](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#-5-personalizaci%C3%B3n)  
📚 [6. Consideraciones](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#-6-consideraciones)

## ➡️ 1. Introducción

En este manual podrás encontrar una guía paso a paso para configurar un proyecto de **[JAVA]** con la pasarela de pagos de IZIPAY. Te proporcionaremos instrucciones detalladas y credenciales de prueba para la instalación y configuración del proyecto, permitiéndote trabajar y experimentar de manera segura en tu propio entorno local.
Este manual está diseñado para ayudarte a comprender el flujo de la integración de la pasarela para ayudarte a aprovechar al máximo tu proyecto y facilitar tu experiencia de desarrollo.

> [!IMPORTANT]
> En la última actualización se agregaron los campos: **nombre del tarjetahabiente** y **correo electrónico** (Este último campo se visualizará solo si el dato no se envía en la creación del formtoken).

<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/formulario_popin/Imagen-Formulario-Popin.png?raw=true" alt="Formulario" width="350"/>
</p>

## 🔑 2. Requisitos Previos

- Comprender el flujo de comunicación de la pasarela. [Información Aquí](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/start.html)
- Extraer credenciales del Back Office Vendedor. [Guía Aquí](https://github.com/izipay-pe/obtener-credenciales-de-conexion)
- Para este proyecto utilizamos Spring Boot v3.2.1.
- Apache Maven 3.9.9
- Java 17 o superior
> [!NOTE]
> Tener en cuenta que, para que el desarrollo de tu proyecto, eres libre de emplear tus herramientas preferidas.

## 🚀 3. Ejecutar ejemplo

### Clonar el proyecto
```sh
git clone https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java
```

### Datos de conexión 

Reemplace **[CHANGE_ME]** con sus credenciales de `API REST` extraídas desde el Back Office Vendedor, revisar [Requisitos previos](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#-2-requisitos-previos).

- Editar el archivo `config.properties` en la ruta `src/main/resources/`:
```java
Archivo para la configuración de las crendeciales de comercio
#
# Identificador de la tienda
merchantCode=CHANGE_ME_USER_ID
# Clave de Test o Producción
password=CHANGE_ME_PASSWORD
# Clave Pública de Test o Producción
publicKey=CHANGE_ME_PUBLIC_KEY
# Clave HMAC-SHA-256 de Test o Producción
hmacKey=CHANGE_ME_HMAC_SHA_256
```

### Ejecutar proyecto

1. Ejecutar el proyecto directamente usando Maven

```sh
mvn spring-boot:run
``` 

2.  Abre un navegador web y navega a la siguiente URL:

```
http://127.0.0.1:8081
```

## 🔗4. Pasos de integración

<p align="center">
  <img src="https://i.postimg.cc/pT6SRjxZ/3-pasos.png" alt="Formulario" />
</p>

## 💻4.1. Desplegar pasarela
### Autentificación
Extraer las claves de `usuario` y `contraseña` del Backoffice Vendedor, concatenar `usuario:contraseña` y agregarlo en la solicitud del encabezado `Authorization`. Podrás encontrarlo en el archivo `src/main/java/com/example/popinpaymentform/controller/McwController.java`.

```java
// Encabezado Basic con concatenación de "usuario:contraseña" en base64
String encoded = Base64.getEncoder().encodeToString((merchantCode+":"+password).getBytes(StandardCharsets.UTF_8));
...
...
String response = webClient.post()
		  .uri("https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment")
		  .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                  ...
                  ...
```
ℹ️ Para más información: [Autentificación](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/embedded/keys.html)
### Crear formtoken
Para configurar la pasarela se necesita generar un formtoken. Se realizará una solicitud API REST a la api de creación de pagos:  `https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment` con los datos de la compra para generar el formtoken. Podrás encontrarlo en el archivo `src/main/java/com/example/popinpaymentform/controller/McwController.java`.

```java
public String generarToken(Map<String, String> parameters) {
    // Definiendo valores para la estructura del Json
    //// Crear el cuerpo de la solicitud JSON
    JSONObject billingDetails = new JSONObject();
    billingDetails.put("firstName", parameters.get("firstName"));
    ...
    ...
    billingDetails.put("zipCode", parameters.get("zipCode"));
	
    JSONObject customer = new JSONObject();
    customer.put("email", parameters.get("email"));
    customer.put("billingDetails", billingDetails);

    JSONObject requestBody = new JSONObject();
    requestBody.put("amount", amount);
    ...
    ...
    requestBody.put("orderId", parameters.get("orderId"));
    
    // Creando la Conexión
	  try {
	  // Encabezado Basic con concatenación de "usuario:contraseña" en base64
	  String encoded = Base64.getEncoder().encodeToString((merchantCode+":"+password).getBytes(StandardCharsets.UTF_8));

    // Crear la conexión a la API para la creación del FormToken
	  WebClient webClient = webClientBuilder.build();
	  String response = webClient.post()
		  .uri("https://api.micuentaweb.pe/api-payment/V4/Charge/CreatePayment")
		  ...
                  ...
		  .block();

	  // Extraemos el FormToken
	  JSONObject jsonResponse = new JSONObject(response); 
	  formToken = jsonResponse.getJSONObject("answer").getString("formToken");
    }

```
ℹ️ Para más información: [Formtoken](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/embedded/formToken.html)
### Visualizar formulario
Para desplegar la pasarela, configura la llave `public key` en el encabezado (Header) del archivo `checkout.html`. Esta llave debe ser extraída desde el Back Office del Vendedor.

Header: 
Se coloca el script de la libreria necesaria para importar las funciones y clases principales de la pasarela. Podrás encontrarlo en el archivo `src/main/resources/templates/checkout.html`.
```javascript
<script type="text/javascript"
src="https://static.micuentaweb.pe/static/js/krypton-client/V4.0/stable/kr-payment-form.min.js"
kr-language="es-Es"
th:attr="kr-public-key=${publicKey}, kr-post-url-success=@{/result}">
</script>

<link rel="stylesheet" href="https://static.micuentaweb.pe/static/js/krypton-client/V4.0/ext/classic.css">
<script type="text/javascript" src="https://static.micuentaweb.pe/static/js/krypton-client/V4.0/ext/classic.js">
</script>
```
Además, se inserta en el body una etiqueta div con la clase `kr-embedded` que deberá tener el atributo `kr-form-token` e incrustarle el `formtoken` generado en la etapa anterior.

Body:
```javascript
<div id="micuentawebstd_rest_wrapper">
       <!-- HTML para incrustar la pasarela de pagos-->
       <div class="kr-embedded" kr-popin th:attr="kr-form-token=${formToken}"></div>	
</div>
```
ℹ️ Para más información: [Visualizar formulario](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/embedded/formToken.html)

## 💳4.2. Analizar resultado del pago

### Validación de firma
Se configura la función `checkHash` que realizará la validación de los datos del parámetro `kr-answer` utilizando una clave de encriptacón definida por el parámetro `kr-hash-key`. Podrás encontrarlo en el archivo `src/main/java/com/example/popinpaymentform/controller/McwController.java`.

```java
public boolean checkHash(String krHash, String krHashKey, String krAnswer){
  	// Obtenemos la Password y la Clave Pública del archivo 'config.properties'
	String passwordKey = properties.getProperty("password");
	String hmacSha256Key = properties.getProperty("hmacKey");
	String key;
	
	// Verifica si la respuesta es de 'Retorno a la tienda' o de la 'IPN'
	if ("sha256_hmac".equals(krHashKey)){
		key = hmacSha256Key;
	} else if ("password".equals(krHashKey)) {
        	key = passwordKey;
  	} else {	
		return false;
  	}
       	
	// Calculamos un Hash usando el valor del 'kr-answer' y el valor del 'kr-hash-key'
	String calculatedHash = HmacSha256(krAnswer, key);
	// Comparamos si el hash es igual y retornamos la respuesta
	return calculatedHash.equals(krHash);

  }
```

Se valida que la firma recibida es correcta. Podrás encontrarlo en el archivo `src/main/java/com/example/popinpaymentform/controller/McwSpringboot.java`.

```java
@PostMapping("/result")
  public String processResult(
	@RequestParam Map<String, String> resultParameters,
  Model model
  ) {
  ...
  ...
  // Válida que la respuesta sea íntegra comprando el hash recibido en el 'kr-hash' con el generado con el 'kr-answer'
  boolean isValidKey = mcwController.checkHash(krHash, krHashKey, krAnswer);
  // Procesa la condicional si la firma es correcta
  if (isValidKey) {
    ...
    ...
  } else {
    break;
  }
```
En caso que la validación sea exitosa, se puede mostrara los datos de `kr-answer` a través de un JSON y mostrar los datos del pago realizado. Podrás encontrarlo en el archivo `src/main/java/com/example/popinpaymentform/controller/McwSpringboot.java`.

```java
@PostMapping("/result")
  public String processResult(
	@RequestParam Map<String, String> resultParameters,
  Model model
  ) {
   // Asignando los valores de la respuesta de Izipay en las variables
   krHash = request.getParameter("kr-hash");
   ...
   ...
   krHashKey = request.getParameter("kr-hash-key");

   // Almacenamos los datos del kr-answer
   jsonResponse = new JSONObject(krAnswer);
   // Convertimos el valor del JSON obtenido a 'pretty print' para su visualización en el template
   String pJson = jsonResponse.toString(4);
		
   // Almacenamos los datos del pago en las variables
   orderStatus = jsonResponse.getString("orderStatus");
   ...
   ...
   String currency = jsonResponse.getJSONObject("orderDetails").getString("orderCurrency");

   // Procesa la condicional si la firma es correcta
   if (isValidKey) {
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
```
ℹ️ Para más información: [Analizar resultado del pago](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/kb/payment_done.html)

### IPN
La IPN es una notificación de servidor a servidor (servidor de Izipay hacia el servidor del comercio) que facilita información en tiempo real y de manera automática cuando se produce un evento, por ejemplo, al registrar una transacción.


Se realiza la verificación de la firma utilizando la función `checkhash` y se devuelve al servidor de izipay un mensaje confirmando el estado del pago. Podrás encontrarlo en el archivo `src/main/java/com/example/popinpaymentform/controller/McwSpringboot.java`.

```java
@PostMapping("/ipn")
  @ResponseBody
  public String processIpn(
  @RequestParam Map<String, String> ipnParameters
  ) {
    // Verifica el orderStatus PAID
    String orderStatus = jsonResponse.getString("orderStatus");
    String orderId = jsonResponse.getJSONObject("orderDetails").getString("orderId");
    String uuid = transactions.getString("uuid");
        
    // Válida que la respuesta sea íntegra comprando el hash recibido en el 'kr-hash' con el generado con el 'kr-answer'
    boolean isValidKey = mcwController.checkHash(krHash, krHashKey, krAnswer);
        
    // Procesa la condicional si la firma es correcta
    if (isValidKey) {
  	// Imprimiendo en la terminal el Order Status
	return "OK! Order Status: " + orderStatus;
```

La IPN debe ir configurada en el Backoffice Vendedor, en `Configuración -> Reglas de notificación -> URL de notificación al final del pago`

<p align="center">
  <img src="https://i.postimg.cc/zfx5JbQP/ipn.png" alt="Formulario" width=80%/>
</p>

ℹ️ Para más información: [Analizar IPN](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/api/kb/ipn_usage.html)

### Transacción de prueba

Antes de poner en marcha su pasarela de pago en un entorno de producción, es esencial realizar pruebas para garantizar su correcto funcionamiento.

Puede intentar realizar una transacción utilizando una tarjeta de prueba con la barra de herramientas de depuración (en la parte inferior de la página).

<p align="center">
  <img src="https://i.postimg.cc/3xXChGp2/tarjetas-prueba.png" alt="Formulario"/>
</p>

- También puede encontrar tarjetas de prueba en el siguiente enlace. [Tarjetas de prueba](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/api/kb/test_cards.html)

## 📡4.3.Pase a producción

Reemplace **[CHANGE_ME]** con sus credenciales de PRODUCCIÓN de `API REST` extraídas desde el Back Office Vendedor, revisar [Requisitos Previos](https://github.com/izipay-pe/Popin-PaymentForm-Springboot-Java/tree/main?tab=readme-ov-file#-2-requisitos-previos).

- Editar el archivo `config.properties` en la ruta `src/main/resources/`:
```java
Archivo para la configuración de las crendeciales de comercio
#
# Identificador de la tienda
merchantCode=CHANGE_ME_USER_ID
# Clave de Test o Producción
password=CHANGE_ME_PASSWORD
# Clave Pública de Test o Producción
publicKey=CHANGE_ME_PUBLIC_KEY
# Clave HMAC-SHA-256 de Test o Producción
hmacKey=CHANGE_ME_HMAC_SHA_256
```

## 🎨 5. Personalización

Si deseas aplicar cambios específicos en la apariencia de la pasarela de pago, puedes lograrlo mediante la modificación de código CSS. En este enlace [Código CSS - Popin](https://github.com/izipay-pe/Personalizacion/blob/main/Formulario%20Popin/Style-Personalization-PopIn.css) podrá encontrar nuestro script para un formulario incrustado.

<p align="center">
  <img src="https://github.com/izipay-pe/Imagenes/blob/main/formulario_popin/Imagen-Formulario-Custom-Popin.png" alt="Formulario"/>
</p>

## 📚 6. Consideraciones

Para obtener más información, echa un vistazo a:

- [Formulario incrustado: prueba rápida](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/quick_start_js.html)
- [Primeros pasos: pago simple](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/javascript/guide/start.html)
- [Servicios web - referencia de la API REST](https://secure.micuentaweb.pe/doc/es-PE/rest/V4.0/api/reference.html)
