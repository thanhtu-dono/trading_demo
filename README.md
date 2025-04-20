## Installation
- Clone project
```bash
git clone url-project
```
## Install Dependency
```bash
gradle build
```
## Start Appplication
```bash
gradle bootRun
```
## Access to Swagger-ui
```bash
http://localhost:8080/swagger-ui/index.html
```

## Note
- User is initialized with information:
  + userId : 1
  + username: tunguyen
  + fullname: Nguyen Thanh Tu
  + email: tunguyen@example.com
  + address: HCM
- Wallet information:
  + "id": 1,
  + "userId": 1,
  + "usdtBalance": 50000,
  + "btcBalance": 0,
  + "ethBalance": 0
 
## API Document
- aggregation-price-controller: get latest price of BTC / ETH
- wallet-controller: get current balance of user
- trade-controller: to trade and view history
