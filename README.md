# ShopView

## Description

A simple online store with simulated orders. Reactive stack.

Available pages:
- Products list
- Product details
- Product creation form
- Shopping cart
- Order list
- Order details

The application allows users to:
- Add new products
- Add or remove products from the cart and adjust their quantities
- Place an order (checkout)

There are two projects:
- Main project with products, car and orders
- Additional project for making payments


## Get started

- Build app docker image and start it
```shell
docker compose up -d --build
```

- Fill test data
```shell
docker-compose run --rm db-init
```

- Main page with product catalog will be available at `http://127.0.0.1:8080/products`
- Login page: http://127.0.0.1:8080/login
```
username: admin
password: password
```

- Swagger for payment service API will be available at: `http://127.0.0.1:8081/swagger-ui.html`
