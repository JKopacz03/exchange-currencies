# Currency Exchange Rates API
This API provides endpoints to calculate currency exchange rates. It connects to a real currency exchange rates API but caches the responses to ensure that the same currency pair is not queried more than once every 10 minutes.

## Features
* Calculate Currency Exchange Rates: Convert an amount from one currency to another.
* Query History: Retrieve a paginated history of all currency exchange queries.
* Reports Generation: Generate various reports based on the exchange history.
## Endpoints
### Calculate Exchange Rate
#### Endpoint:

* POST /api/v1/calculator/_exchange

#### Request Body:

* amount: The amount to be converted.
* currencyFrom: The source currency code.
* currencyTo: The target currency code.

### Retrieve Exchange Query History
#### Endpoint:

GET /api/v1/calculator/exchanges

### Generate Reports
#### Endpoint:

GET /api/v1/reports/{reportName}

#### Supported Reports:

#### HIGH_AMOUNT_LAST_MONTH

Returns transactions from the last month where the total value exceeded 15,000 euros on the transaction date.
Example Response:

currencyFrom: The source currency code.
totalAmount: The total amount of transactions.
#### GROUP_BY_CURRENCY_FROM_LAST_MONTH

Returns transactions from the last month grouped by the source currency.
Example Response:

currencyFrom: The source currency code.
count: The number of transactions.
#### GROUP_BY_CURRENCY_TO_LAST_MONTH

Returns transactions from the last month grouped by the target currency.
Example Response:

currencyTo: The target currency code.
count: The number of transactions.
