# HLA_Shopping_Sim

## RTI-based simulation project

---

### **Content**

#### 1. [Description](#description)

#### 2. [Run](#run)

#### 3. [Federates](#federates)

#### 4. [Interactions](#interactions)

#### 5. [Constant simulation variables](#constant-simulation-variables)

#### 6. [Example output](#example-output)

---

## **Description**

### Simulation context

There are specified **cash registers** in the **store**.
**Clients** are shopping for a random amount of time and buy random amount of products.
After that they are lining up to the cash register, what has the shortest queue.
Client's service time is linearly dependent on his amount of bought products.

### Tasks

1. Estimate the mean waiting time in queue.
2. Estimate the mean queue length for each cash register.
3. Determine which cash register served the most of the clients.

---

## **Run**

To run the solution you need to start 4 processes:

- StatisticsFederate
- StoreFederate
- CashesFederate
- ClientsFederate

After all processes starts, you have to press *ENTER* on each process's terminal.

Then the whole federation synchronize its simulation time and start main loop.

---

## **Federates**

### There are 4 federates developed in this solution.

### Store Federate

It is responsible for sending clients, who ended up their shopping process, to
the shortest queues.

### Cashes Federate

It is responsible for registering new cash registers and handling client service.

### Clients Federate

It is responsible for generating new clients, who arrive to the store and determining, 
when each client ends up his shopping process.

### Statistics Federate

It is responsible for simulation statistics calculations.

It only subscribes appropriate interactions and print statistics summary 
at the end of simulation.

---

## **Interactions**

Interaction reports that new cash register is available to service.

### New Cash Register

**Publisher:**

- Cashes Federate

**Subscribers:**

- Store Federate
- Statistics Federate

**Parameters:**

``` java
int cashId;     // cash register ID
```


### New Client Arrival

Interaction reports that new client has already arrived to the store. 

**Publisher:**

- Clients Federate

**Subscribers:**

- Store Federate
- Statistics Federate

**Parameters:**

``` java
int clientId;       // client ID
int goodsAmount;    // amount of products that client bought
```


### Client Shopping End

Interaction reports that the specified client has ended his shopping process.

**Publisher:**

- Clients Federate

**Subscribers:**

- Store Federate

**Parameters:**

``` java
int clientId;       // client ID
```


### Client Queue Get

Interaction indicates queue that specified client get into.

**Publisher:**

- Store Federate

**Subscribers:**

- Cashes Federate
- Statistics Federate

**Parameters:**

``` java
int cashId;         // cash register ID
int clientId;       // client ID
int goodsAmount;    // amount of products that client bought
```


### Client Service Start

Interaction reports that specified cash register has started to service the first client from its queue.

**Publisher:**

- Cashes Federate

**Subscribers:**

- Store Federate
- Statistics Federate

**Parameters:**

``` java
int cashId;         // cash register ID
int clientId;       // client ID
```


### Client Service End

Interaction reports that specified cash register has ended servicing the first client from its queue.

**Publisher:**

- Cashes Federate

**Subscribers:**

- Store Federate
- Statistics Federate

**Parameters:**

``` java
int cashId;         // cash register ID
int clientId;       // client ID
```

---

## **Constant simulation variables**

`SIMULATION_TIME` - located in every *`*Federate.java`* file and indicates the maximal simulation 
time to end of the federate process work..

`MIN_SHOPPING_TIME` - located in `ClientsManager.java` and indicates the minimal
time of client's shopping process.

`MAX_SHOPPING_TIME` - located in `ClientsManager.java` and indicates the maximal
time of client's shopping process.

`CLIENT_SERVICE_TIME_FACTOR` - located in `CashesManager.java` and indicates the factor
of client service time. Client service time is equal to `client.goodsAmount` multiply `CLIENT_SERVICE_TIME_FACTOR`.

`CASHES_MAX_COUNT` - located in `CashesManager.java` and indicates the maximal amount
of cash registers.

`MAX_GOODS_AMOUNT` - located int `Client.java` and indicates the maximal amount
of products client can buy.

---

## **Example output**

### Conditions

Example output is produced with the following set of sumulation variables:

``` java
// Each *Federate.java file
double SIMULATION_TIME = 2000.0;

// ClientsManager.java
double MIN_SHOPPING_TIME = 50.0;
double MIN_SHOPPING_TIME = 500.0;

// CashesManager.java
double CLIENT_SERVICE_TIME_FACTOR = 50.0;
int CASHES_MAX_COUNT = 6;

// Client.java
int MAX_GOODS_AMOUNT = 20;
```

### Statistics

```text
 --- STATISTICS SUMMARY ---

 Mean queue len: 
  - 0 :: 2.77
  - 1 :: 2.91
  - 2 :: 1.60
  - 3 :: 2.00
  - 4 :: 2.72
  - 5 :: 2.81
  - 6 :: 1.50
  - 7 :: 1.50
 * Average: 2.23

 Mean waiting time: 
  - 0 :: 591.00
  - 1 :: 1261.27
  - 2 :: 241.00
  - 3 :: 251.00
  - 4 :: 1239.28
  - 5 :: 1168.50
  - 6 :: 0.00
  - 7 :: 0.00
 * Average: 594.01

 Max serviced clients count: 
   * cash ID:           1
   * serviced clients:  37
```
