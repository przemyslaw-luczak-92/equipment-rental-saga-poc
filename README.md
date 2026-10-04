# Equipment Rental – Eventuate Tram Saga POC

Techniczny POC wypożyczalni sprzętu z dwiema usługami, osobnymi bazami PostgreSQL, komunikacją przez RabbitMQ oraz sagą orkiestracyjną Eventuate Tram.

## Cel

Projekt demonstruje:

- architekturę heksagonalną;
- database per service;
- transactional outbox;
- CDC (Change Data Capture);
- asynchroniczne komendy i odpowiedzi przez RabbitMQ;
- orkiestrację sagi;
- kompensację;
- idempotencję techniczną i biznesową;
- blokady pesymistyczne;
- kontynuowanie procesu po restarcie usługi.

Projekt nie obiecuje ogólnego `exactly-once`. Zakłada możliwość ponownego dostarczenia wiadomości i chroni skutki biznesowe przed duplikacją.

## Dlaczego Eventuate Tram?

Najtrudniejszym problemem w tym projekcie nie jest samo wysłanie wiadomości
przez RabbitMQ. Problemem jest niezawodne połączenie lokalnych transakcji
bazodanowych z komunikacją asynchroniczną oraz utrzymanie stanu procesu,
który obejmuje kilka mikroserwisów.

Eventuate Tram dostarcza gotowe mechanizmy potrzebne do rozwiązania tego
problemu:

- transactional outbox, dzięki któremu zmiana biznesowa i wiadomość są
  zapisywane w tej samej lokalnej transakcji;
- integrację z CDC, które odczytuje zatwierdzone wiadomości z outboxa
  i publikuje je do RabbitMQ;
- model komunikacji oparty na komendach i odpowiedziach;
- routing komend do odpowiednich kanałów i handlerów;
- korelację odpowiedzi z właściwą instancją sagi;
- trwały zapis stanu sagi w bazie danych;
- kontynuowanie procesu po restarcie aplikacji;
- deklarowanie kolejnych kroków oraz kompensacji za pomocą Saga DSL;
- techniczną deduplikację ponownie dostarczonej wiadomości na podstawie
  jej `messageId`.

Dzięki temu kod projektu może koncentrować się przede wszystkim na logice
biznesowej:

- kiedy zarezerwować sprzęt;
- kiedy potwierdzić booking;
- kiedy zwolnić wcześniej zarezerwowany stan;
- z jakiego powodu odrzucić booking.

Bez Eventuate Tram musielibyśmy samodzielnie przygotować między innymi:

- strukturę tabeli outbox;
- kod zapisujący wiadomości w tej samej transakcji co dane biznesowe;
- proces odczytujący outbox i publikujący wiadomości do RabbitMQ;
- format komend, odpowiedzi i nagłówków korelacyjnych;
- dispatchery kierujące wiadomości do właściwych handlerów;
- mechanizm przechowywania aktualnego kroku sagi;
- obsługę odpowiedzi sukcesu i błędów biznesowych;
- uruchamianie kompensacji w odwrotnej kolejności;
- odtwarzanie procesu po restarcie;
- techniczną deduplikację wiadomości;
- znaczną część obsługi ponowień, blokad i sytuacji częściowej awarii.

| Obszar | Z Eventuate Tram | Bez Eventuate Tram |
|---|---|---|
| Outbox | Gotowa integracja z lokalną transakcją | Własna tabela i kod zapisu |
| Publikowanie wiadomości | CDC odczytuje outbox | Własny publisher lub polling worker |
| Komendy i odpowiedzi | Gotowy model oraz dispatchery | Własny format, routing i korelacja |
| Stan sagi | Trwale przechowywany przez framework | Własna maszyna stanów i tabele |
| Kompensacje | Deklarowane w definicji sagi | Własny mechanizm wyboru i kolejności |
| Restart aplikacji | Saga może kontynuować proces | Własne odtwarzanie niedokończonych procesów |
| Duplikaty wiadomości | Techniczna deduplikacja `messageId` | Własna tabela i logika deduplikacji |

Eventuate Tram nie usuwa jednak odpowiedzialności z kodu biznesowego.
Framework nie zdecyduje za nas:

- jakie kroki powinien zawierać proces;
- co jest błędem biznesowym, a co technicznym;
- jak powinna wyglądać kompensacja;
- jakie reguły obowiązują w domenie;
- jak zabezpieczyć skutki biznesowe przed powtórzeniami.

Nie daje również ogólnej gwarancji `exactly-once`. Wiadomość może zostać
dostarczona ponownie, dlatego nadal potrzebujemy idempotentnych operacji,
takich jak `InventoryHold`, stabilne stany domenowe i blokady
pesymistyczne.

W tym POC Eventuate Tram pełni więc rolę infrastrukturalnego silnika
niezawodnej komunikacji i orkiestracji, natomiast decyzje biznesowe
pozostają w naszych klasach domenowych i serwisach aplikacyjnych.

## Moduły

```text
equipment-rental-parent
├── integration-contracts
├── booking-service
└── inventory-service
```

### `integration-contracts`

Zawiera wyłącznie kontrakty komunikacji Eventuate:

- komendy;
- odpowiedzi sukcesu;
- odpowiedzi błędów biznesowych.

Nie zawiera encji JPA, repozytoriów ani logiki domenowej.

### `booking-service`

Odpowiada za:

- przyjęcie żądania rezerwacji;
- agregat `Booking`;
- limit klienta;
- orkiestrator `CreateBookingSaga`;
- potwierdzenie albo odrzucenie rezerwacji.

### `inventory-service`

Odpowiada za:

- `StockItem`;
- `InventoryHold`;
- rezerwowanie sprzętu;
- zwalnianie blokady;
- ochronę dostępności przy współbieżności.

Każda usługa używa wyłącznie własnej bazy.

## Architektura przepływu

```text
Client
  │
  │ POST /bookings
  ▼
booking-service
  │
  ├── booking-db: Booking PENDING
  ├── booking-db: saga instance
  └── booking-db: Eventuate outbox
                         │
                         ▼
                       CDC
                         │
                         ▼
                     RabbitMQ
                         │
                         ▼
                 inventory-service
                         │
                         ├── inventory-db: StockItem
                         ├── inventory-db: InventoryHold
                         └── inventory-db: reply outbox
                                               │
                                               ▼
                                             CDC
                                               │
                                               ▼
                                           RabbitMQ
                                               │
                                               ▼
                                  CreateBookingSaga continues
```

## Wersje

| Element | Wersja |
|---|---:|
| Java | 25 |
| Spring Boot | 4.1.1 |
| Maven | 3.9.16 |
| Eventuate Platform BOM | 2026.0.RELEASE |
| Eventuate Tram | 0.37.0.RELEASE |
| Eventuate Tram Sagas | 0.26.0.RELEASE |
| Eventuate CDC | 0.19.0.RELEASE |
| Eventuate PostgreSQL image | 0.21.0.RELEASE |
| RabbitMQ | 4.1.8-management |
| ZooKeeper | 3.9.5 |
| OpenAPI Generator | 7.25.0 |

## Porty

| Element | Port |
|---|---:|
| booking-service | 8081 |
| inventory-service | 8082 |
| booking-db | 5433 |
| inventory-db | 5434 |
| RabbitMQ AMQP | 5672 |
| RabbitMQ Management | 15672 |
| ZooKeeper | 2181 |
| Eventuate CDC | 8099 |

RabbitMQ Management:

```text
http://localhost:15672
```

Lokalne dane logowania:

```text
guest / guest
```

## Wymagania

- JDK 25;
- Maven 3.9+;
- Docker Desktop;
- Docker Compose;
- wolne porty wymienione powyżej.

Sprawdzenie środowiska:

```powershell
java -version
mvn -version
docker version
docker compose version
```

Jeśli Maven używa niewłaściwego JDK:

```powershell
$env:JAVA_HOME = "C:\Users\przem\.jdks\openjdk-25.0.1"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

## Budowanie projektu

```powershell
mvn clean verify
```

Oczekiwany wynik:

```text
booking-service:   33 testy
inventory-service: 27 testów
łącznie:           60 testów
BUILD SUCCESS
```

## Uruchomienie infrastruktury

```powershell
docker compose up -d
docker compose ps
```

Oczekiwane kontenery:

- `booking-db`;
- `inventory-db`;
- `rabbitmq`;
- `zookeeper`;
- `cdc`.

## Uruchomienie aplikacji

Można uruchomić klasy aplikacyjne z IntelliJ:

1. `InventoryServiceApplication`;
2. `BookingServiceApplication`.

Alternatywnie, po wykonaniu `mvn clean verify`, w dwóch terminalach:

```powershell
java -jar inventory-service\target\inventory-service-0.0.1-SNAPSHOT.jar
```

```powershell
java -jar booking-service\target\booking-service-0.0.1-SNAPSHOT.jar
```

## REST API

Kontrakty znajdują się w:

```text
booking-service/src/main/resources/openapi/booking-api.yaml
inventory-service/src/main/resources/openapi/inventory-api.yaml
```

| Metoda | Endpoint | Znaczenie |
|---|---|---|
| POST | `/bookings` | Tworzy booking `PENDING` i uruchamia sagę |
| GET | `/bookings/{bookingId}` | Zwraca bieżący stan bookingu |
| GET | `/equipment/{equipmentId}` | Zwraca stan magazynowy |

`POST /bookings` zwraca `202 Accepted`, ponieważ saga kończy się asynchronicznie. Klient może następnie odpytywać `GET /bookings/{bookingId}`.

## Scenariusz 1 – sukces

Żądanie:

```http
POST http://localhost:8081/bookings
Content-Type: application/json
```

```json
{
  "customerId": "customer-normal",
  "equipmentId": "camera",
  "quantity": 2
}
```

Oczekiwany wynik końcowy:

- `Booking CONFIRMED`;
- `InventoryHold HELD`;
- `camera.held = 2`;
- saga zakończona bez kompensacji.

## Scenariusz 2 – niewystarczający stan

Po wykonaniu scenariusza sukcesu:

```json
{
  "customerId": "customer-normal",
  "equipmentId": "camera",
  "quantity": 4
}
```

Dostępne są tylko 3 sztuki.

Oczekiwany wynik:

- `InventoryHold REFUSED`;
- reason code `INSUFFICIENT_STOCK`;
- `Booking REJECTED`;
- `camera.held` pozostaje równe `2`;
- `ReleaseStockCommand` nie jest wykonywane, ponieważ blokada nie powstała.

## Scenariusz 3 – kompensacja

```json
{
  "customerId": "customer-compensation",
  "equipmentId": "camera",
  "quantity": 2
}
```

Klient ma limit równy `0`.

Przebieg:

1. `ReserveStockCommand` tworzy `InventoryHold HELD`.
2. `ConfirmBookingCommand` zwraca `CUSTOMER_LIMIT_EXCEEDED`.
3. Saga wysyła `ReleaseStockCommand`.
4. Hold przechodzi do `RELEASED`.
5. Saga wysyła `RejectBookingCommand`.
6. Booking kończy jako `REJECTED`.

Kompensacja jest nową transakcją biznesową, a nie rollbackiem wcześniejszej transakcji.

## Współbieżność

Dane `tripod` mają:

```text
total=5
held=0
```

Dwa równoległe żądania po 4 sztuki kończą się:

- jednym `Booking CONFIRMED` i `InventoryHold HELD`;
- jednym `Booking REJECTED` i `InventoryHold REFUSED`;
- `tripod.held = 4`;
- `held <= total`.

`StockItem` jest pobierany z blokadą `PESSIMISTIC_WRITE`. Druga transakcja czeka, a po uzyskaniu blokady widzi stan zapisany przez pierwszą.

## Restart procesu

Test restartu wykorzystuje `restart-kit` oraz klienta `customer-compensation`.

1. Zatrzymaj `inventory-service`.
2. Utwórz booking dla `restart-kit`.
3. Zatrzymaj `booking-service`.
4. Uruchom `inventory-service`.
5. Sprawdź `InventoryHold HELD` i `Booking PENDING`.
6. Uruchom `booking-service`.
7. Sprawdź końcowe `InventoryHold RELEASED` i `Booking REJECTED`.

Stan procesu nie ginie, ponieważ saga, outboxy i dane biznesowe są trwałe, a odpowiedź oczekuje w RabbitMQ.

## Granice transakcji

### Utworzenie bookingu

Jedna lokalna transakcja w `booking-db` obejmuje:

- zapis `Booking PENDING`;
- utworzenie instancji sagi;
- zapis pierwszej komendy do outboxa.

Nie ma transakcji obejmującej obie bazy.

### Obsługa komendy inventory

Lokalna transakcja w `inventory-db` obejmuje:

- techniczną deduplikację `messageId`;
- blokadę `InventoryHold`;
- blokadę `StockItem`;
- zmianę biznesową;
- zapis odpowiedzi do outboxa.

### Potwierdzenie bookingu

Lokalna transakcja w `booking-db` obejmuje:

- blokadę `Booking`;
- blokadę `CustomerQuota`;
- zmianę statusu;
- zwiększenie `confirmedCount`;
- zapis odpowiedzi do outboxa.

## Idempotencja

Rozróżniamy dwa przypadki:

1. Ten sam `messageId` – Eventuate wykorzystuje `received_messages` do deduplikacji technicznej.
2. Nowa wiadomość z nowym `messageId`, ale tym samym `bookingId` – chroni ją logika biznesowa i unikalny `InventoryHold.bookingId`.

Dodatkowo:

- ponowiony Reserve nie zwiększa `held`;
- ponowiony Release nie zmniejsza `held`;
- `REFUSED` pozostaje stabilnym wynikiem;
- ponowiony Confirm nie zwiększa drugi raz limitu klienta;
- `RELEASED` nie może zostać ponownie aktywowany.

## Logi korelacyjne

Handlery logują `bookingId`, dzięki czemu można prześledzić proces między usługami:

```text
RESERVE_STOCK_PROCESSED
CONFIRM_BOOKING_PROCESSED
RELEASE_STOCK_PROCESSED
REJECT_BOOKING_PROCESSED
```

## Świadome ograniczenia POC

Projekt nie zawiera:

- API Gateway;
- security i OAuth;
- HTTP `Idempotency-Key`;
- CQRS;
- event sourcing;
- końcowych zdarzeń `BookingConfirmed` i `BookingRejected`;
- timeoutów sagi;
- procesu reconciliation;
- rozbudowanego retry, DLQ i replay;
- distributed tracing;
- Prometheusa i Grafany;
- Kubernetes;
- CI/CD;
- pełnego zestawu Testcontainers;
- obsługi wielu pozycji w jednej rezerwacji;
- terminów wypożyczenia, cen i płatności.

Brak odpowiedzi uczestnika nie oznacza automatycznie odrzucenia. Bez timeoutu saga pozostaje oczekująca, ponieważ wynik operacji jest nieznany.

Błędy biznesowe, takie jak `INSUFFICIENT_STOCK`, są częścią przepływu sagi. Błąd bazy lub niedostępność infrastruktury są błędami technicznymi i nie mogą być zamieniane na odmowę biznesową.