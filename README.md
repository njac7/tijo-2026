# Symulator giełdy z silnikiem dopasowywania zleceń

## Nazwa kursu

Testowanie i Jakość Oprogramowania

## Autor

Norbert Armatys (37664), grupa PT2

## Temat projektu

Symulator giełdy z silnikiem dopasowywania zleceń kupna i sprzedaży.

## Opis projektu

Aplikacja webowa, która symuluje działanie giełdy papierów wartościowych. Inwestorzy mają konta z gotówką
i akcjami trzech fikcyjnych spółek (ALFA, BETA, GAMA). Składają zlecenia kupna i sprzedaży, a silnik
dopasowywania paruje je ze zleceniami innych inwestorów i od razu rozlicza transakcje: przenosi akcje
i gotówkę między kontami oraz aktualizuje kurs spółki.

### Ekrany

- **Rynek** – karty spółek z kursem, zmianą procentową i wykresem, tabela notowań (kurs, zmiana,
  kurs odniesienia, min, max, najlepsza oferta kupna i sprzedaży, wolumen) i przewijany pasek notowań.
- **Spółka** – kurs ze statystykami, wykres ceny, arkusz zleceń z głębokością rynku, formularz zlecenia
  i ostatnie transakcje. Kliknięcie ceny w arkuszu wpisuje ją do formularza. Formularz na bieżąco liczy
  wartość zlecenia i ostrzega, gdy przekracza ona wolne środki albo liczbę wolnych akcji.
- **Portfel** – wartość portfela, gotówka całkowita, wolna i zablokowana, posiadane akcje, aktywne
  zlecenia z postępem wykonania i przyciskiem anulowania oraz zlecenia zakończone.
- **Transakcje** – historia transakcji na całym rynku.

Inwestora zmienia się w prawym górnym rogu, więc jedna osoba może zagrać po obu stronach transakcji.

### Reguły działania giełdy

1. **Priorytet ceny i czasu.** Zlecenie kupna dopasowywane jest najpierw do najtańszej oferty sprzedaży,
   a zlecenie sprzedaży do najwyższej oferty kupna. Przy tej samej cenie pierwszeństwo ma zlecenie złożone wcześniej.
2. **Cena transakcji** to cena zlecenia, które czekało w arkuszu.
3. **Zlecenie z limitem** wykonuje się tylko po cenie nie gorszej niż limit. Niewykonana część czeka w arkuszu.
4. **Zlecenie PKC** (po każdej cenie) wykonuje się od razu po najlepszych dostępnych cenach. Niewykonana
   część jest anulowana i nie trafia do arkusza.
5. **Częściowe wykonanie.** Zlecenie może być realizowane w kilku transakcjach z różnymi zleceniami.
6. **Blokada środków.** Kupno z limitem blokuje `ilość × limit` gotówki, a sprzedaż blokuje akcje.
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
Maria Zielińska) z gotówką i akcjami, historia kilkunastu transakcji oraz zlecenia w arkuszach.
Baza H2 działa w pamięci, więc każde uruchomienie zaczyna od tego samego stanu. Dane startowe
można wyłączyć właściwością `app.seed-data=false`.

### Struktura projektu

```
src/main/java/pl/tijo/exchange
├── domain       encje: Company, Trader, Holding, Order, Trade
├── engine       MatchingEngine – dopasowywanie zleceń (czysta logika, bez bazy)
├── service      OrderService (składanie, rozliczanie, anulowanie), MarketService (odczyt), OrderValidator
├── repository   repozytoria Spring Data JPA
├── config       zegar aplikacji i dane startowe
└── web          kontrolery stron Thymeleaf i REST API
```

## Uruchomienie projektu

Wymagana jest Java 21. Maven nie musi być zainstalowany, bo projekt zawiera Maven Wrapper.

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

## Testy

Uruchomienie wszystkich testów:

```
mvnw.cmd test
```

Opis testów jednostkowych i integracyjnych z odnośnikami do plików zostanie dodany w etapie 2.

## Dokumentacja API

| Metoda | Adres | Opis |
|---|---|---|
| GET | `/api/companies` | lista spółek z kursem, zmianą, najlepszymi ofertami i wolumenem |
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

## Przypadki testowe dla testera manualnego (TestCase)

Przypadki testowe zostaną dodane w etapie 3.

## Technologie użyte w projekcie

- Java 21
- Spring Boot 4.2 (Spring MVC, Spring Data JPA, Validation)
- Thymeleaf (interfejs użytkownika)
- H2 (baza danych w pamięci)
- Maven (z dołączonym Maven Wrapperem)
- JUnit 5
