# Symulator giełdy z silnikiem dopasowywania zleceń

Projekt zaliczeniowy z przedmiotu **Testowanie i Jakość Oprogramowania**.

Autor: Norbert Armatys (37664), grupa PT2

## Opis projektu

Aplikacja webowa, która symuluje działanie giełdy papierów wartościowych. Inwestorzy mają konta z gotówką
i akcjami trzech fikcyjnych spółek (ALFA, BETA, GAMA). Składają zlecenia kupna i sprzedaży, a silnik
dopasowywania paruje je ze zleceniami innych inwestorów i od razu rozlicza transakcje: przenosi akcje
i gotówkę między kontami oraz aktualizuje ostatnią cenę spółki.

Funkcje:

- notowania spółek: ostatnia cena, najlepsza oferta kupna i sprzedaży,
- arkusz zleceń spółki z poziomami cenowymi i spreadem,
- składanie zleceń kupna i sprzedaży z limitem ceny albo po każdej cenie (PKC),
- portfel inwestora: gotówka całkowita, zablokowana i wolna, akcje, wartość portfela,
- lista aktywnych i zakończonych zleceń z możliwością anulowania aktywnych,
- historia transakcji,
- przełączanie inwestora w nagłówku strony, żeby jedna osoba mogła zagrać po obu stronach transakcji,
- REST API z tymi samymi operacjami.

### Reguły działania giełdy

1. **Priorytet ceny i czasu.** Zlecenie kupna dopasowywane jest najpierw do najtańszej oferty sprzedaży,
   zlecenie sprzedaży do najwyższej oferty kupna. Przy tej samej cenie pierwszeństwo ma zlecenie złożone wcześniej.
2. **Cena transakcji** to cena zlecenia, które czekało w arkuszu.
3. **Zlecenie z limitem** wykonuje się tylko po cenie nie gorszej niż limit. Niewykonana część czeka w arkuszu.
4. **Zlecenie PKC** wykonuje się od razu po najlepszych dostępnych cenach. Niewykonana część jest anulowana
   i nie trafia do arkusza.
5. **Częściowe wykonanie.** Zlecenie może być realizowane w kilku transakcjach z różnymi zleceniami.
6. **Blokada środków.** Kupno z limitem blokuje `ilość × limit` gotówki, sprzedaż blokuje akcje.
   Jeśli transakcja zajdzie po cenie niższej niż limit, różnica wraca do wolnych środków.
7. **Brak pokrycia.** Nie można kupić za więcej, niż wynoszą wolne środki, ani sprzedać akcji,
   których się nie ma albo które są już zablokowane w innym zleceniu.
8. **Anulowanie.** Anulować można tylko własne, aktywne zlecenie. Anulowanie zwalnia zablokowane środki
   niewykonanej części.
9. **Brak transakcji z samym sobą.** Silnik pomija zlecenia tego samego inwestora po drugiej stronie arkusza.
10. **Walidacja.** Ilość akcji to liczba całkowita od 1 do 1 000 000. Cena jest dodatnia, ma najwyżej
    2 miejsca po przecinku (krok 0,01 zł) i nie przekracza 1 000 000 zł.

### Dane startowe

Po uruchomieniu w bazie są 3 spółki, 4 inwestorów (Anna Kowalska, Jan Nowak, Piotr Wiśniewski,
Maria Zielińska) z gotówką i akcjami oraz kilka zleceń w arkuszach, żeby od razu dało się handlować.
Baza H2 działa w pamięci, więc każde uruchomienie zaczyna od tego samego stanu.

## Technologie

- Java 21
- Spring Boot 4.2 (Spring MVC, Spring Data JPA, Validation)
- Thymeleaf (interfejs użytkownika)
- H2 (baza danych w pamięci)
- Maven (z dołączonym Maven Wrapperem)
- JUnit 5

## Uruchomienie projektu

Wymagana jest Java 21. Maven nie musi być zainstalowany, bo projekt zawiera wrapper.

Windows:

```
mvnw.cmd spring-boot:run
```

Linux / macOS:

```
./mvnw spring-boot:run
```

Z zainstalowanym Mavenem:

```
mvn spring-boot:run
```

Aplikacja działa pod adresem [http://localhost:8080](http://localhost:8080).
Konsola bazy danych H2: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
(JDBC URL `jdbc:h2:mem:exchange`, użytkownik `sa`, bez hasła).

## Uruchomienie testów

```
mvnw.cmd test
```

## Dokumentacja API

| Metoda | Adres | Opis |
|---|---|---|
| GET | `/api/companies` | lista spółek z ostatnią ceną i najlepszymi ofertami |
| GET | `/api/companies/{ticker}/orderbook` | arkusz zleceń spółki (poziomy kupna i sprzedaży) |
| GET | `/api/companies/{ticker}/trades` | ostatnie transakcje spółki |
| GET | `/api/trades` | historia wszystkich transakcji |
| GET | `/api/traders` | lista inwestorów z gotówką |
| GET | `/api/traders/{id}/portfolio` | portfel inwestora: akcje, zlecenia, wartość |
| POST | `/api/orders` | złożenie zlecenia |
| DELETE | `/api/orders/{id}?traderId={traderId}` | anulowanie zlecenia |

Przykład złożenia zlecenia:

```
POST /api/orders
Content-Type: application/json

{
  "traderId": 3,
  "ticker": "ALFA",
  "side": "BUY",
  "type": "LIMIT",
  "quantity": 50,
  "limitPrice": 51.00
}
```

Pole `side` przyjmuje `BUY` albo `SELL`, pole `type` przyjmuje `LIMIT` albo `MARKET`
(dla `MARKET` pole `limitPrice` jest pomijane). Odpowiedź `201 Created` zawiera zlecenie ze statusem
`NEW`, `PARTIALLY_FILLED`, `FILLED` albo `CANCELLED`. Naruszenie reguły zwraca `400 Bad Request`
z opisem w polu `detail`, a nieistniejąca spółka, inwestor lub zlecenie zwraca `404 Not Found`.

## Struktura projektu

```
src/main/java/pl/tijo/exchange
├── domain       encje: Company, Trader, Holding, Order, Trade
├── engine       MatchingEngine – dopasowywanie zleceń (czysta logika, bez bazy)
├── service      OrderService (składanie, rozliczanie, anulowanie), MarketService (odczyt), walidacja
├── repository   repozytoria Spring Data JPA
├── config       zegar aplikacji i dane startowe
└── web          kontrolery stron Thymeleaf i REST API
```
