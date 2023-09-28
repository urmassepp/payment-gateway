# Payment Gateway

Gateway between payment provider and merchant. The gateway listens to /api/v1/sms endpoint, forwards received information to merchant and sends reply message with data received from the merchant. The port the application listens to is configurable, by default 8080. Update application.properties as needed.

Tested in IntelliJ IDEA (Run PaymentGatewayApplication).