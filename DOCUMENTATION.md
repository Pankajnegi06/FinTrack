# FinTrack: Project Documentation

## 1. Introduction

FinTrack is a Java console application for managing personal expenses. A user can add, view, delete and filter expenses, set a monthly budget, check how much of it is left, and generate a simple spending report. Data is saved in files so it is kept between runs.

The main aim of the project is to apply Object-Oriented Programming concepts in Java: classes and objects, encapsulation, abstraction, inheritance, polymorphism, interfaces, enums, generics, collections, streams, exception handling, file handling and unit testing.

## 2. Objectives

- Build a practical, modular application using OOP principles.
- Store and process expense records using collections and generics.
- Handle invalid input with custom exceptions.
- Save and load data using file handling.
- Test important methods using JUnit.
- Follow clean code and SOLID principles.

## 3. Package Structure

| Package | Purpose | Classes |
|---|---|---|
| `com.fintrack.model` | Data classes | `Transaction`, `Expense`, `RecurringExpense`, `Budget`, `Category` |
| `com.fintrack.exception` | Application-specific errors | `InvalidAmountException`, `ExpenseNotFoundException`, `BudgetExceededException` |
| `com.fintrack.storage` | Saving and loading data | `DataStore<T>`, `ExpenseFileStore`, `BudgetFileStore` |
| `com.fintrack.service` | Business logic | `ReportGenerator`, `ExpenseManager` |
| `com.fintrack.ui` | User interface | `Main` |

## 4. Class Diagram

```
                 +---------------------------+
                 |  <<abstract>> Transaction |
                 |---------------------------|
                 | - id : int                |
                 | - amount : double         |
                 | - date : LocalDate        |
                 |---------------------------|
                 | + getSummary() : String   |
                 | + toFileString() : String |
                 +---------------------------+
                               ^
                               |
                 +---------------------------+        +------------------+
                 |          Expense          |------->| <<enum>>         |
                 |---------------------------|        |  Category        |
                 | - category : Category     |        |------------------|
                 | - description : String    |        | FOOD, TRANSPORT, |
                 +---------------------------+        | SHOPPING, BILLS, |
                               ^                      | ENTERTAINMENT,   |
                               |                      | HEALTH,          |
                 +---------------------------+        | EDUCATION, OTHER |
                 |      RecurringExpense     |        +------------------+
                 |---------------------------|
                 | - intervalDays : int      |
                 +---------------------------+


 +----------------------+        +---------------------------+
 | <<interface>>        |        | <<interface>>             |
 |  ReportGenerator     |        |  DataStore<T>             |
 |----------------------|        |---------------------------|
 | + generateReport()   |        | + saveAll(List<T>)        |
 +----------------------+        | + loadAll() : List<T>     |
            ^                    +---------------------------+
            |                                ^
 +----------------------+                    |
 |    ExpenseManager    |        +---------------------------+
 |----------------------|        |     ExpenseFileStore      |
 | - expenses : List    |        +---------------------------+
 | - budget : Budget    |
 | - nextId : int       |        +---------------------------+
 +----------------------+        |     BudgetFileStore       |
                                 +---------------------------+
```

`ExpenseManager` uses `Expense`, `Budget` and `Category`. `Main` uses `ExpenseManager`, `ExpenseFileStore` and `BudgetFileStore`.

## 5. Class Descriptions

### model

**Transaction** (abstract class): Base class holding `id`, `amount` and `date`. It declares two abstract methods, `getSummary()` and `toFileString()`, which every subclass must implement.

**Expense**: Extends `Transaction` and adds `category` and `description`. It has two constructors (with and without a date; the second uses `this(...)` to call the first). It overrides `getSummary()` to give a formatted table row and `toFileString()` to give the line saved to file.

**RecurringExpense**: Extends `Expense` and adds `intervalDays`. It uses `super(...)` in its constructor and `super.getSummary()` to extend the parent's summary text. This is the polymorphism example: the same method call gives a different result depending on the object type.

**Budget**: Holds the monthly limit. `getRemaining(spent)` returns the limit minus the amount spent, and `isSet()` tells whether a budget has been set.

**Category** (enum): The expense categories, each with a display label. `fromString()` converts text read from a file back to the enum value.

### exception

| Exception | Thrown when |
|---|---|
| `InvalidAmountException` | The amount, budget, or recurring interval is zero or negative |
| `ExpenseNotFoundException` | A delete is attempted with an ID that does not exist |
| `BudgetExceededException` | Spending for a month is more than the budget. It carries the extra amount through `getExceededBy()` |

All three are checked exceptions, so the compiler forces the calling code to handle them.

### storage

**DataStore<T>**: Generic interface with `saveAll` and `loadAll`. Any storage type (file, database) could implement it.

**ExpenseFileStore**: Implements `DataStore<Expense>`. Writes one line per expense and reads them back, creating a `RecurringExpense` or a plain `Expense` depending on the type field. Uses try-with-resources so files are always closed.

**BudgetFileStore**: Saves and loads the budget amount. If the file does not exist, an empty `Budget` is returned.

### service

**ReportGenerator**: Interface with one method, `generateReport()`.

**ExpenseManager**: Contains the business logic and implements `ReportGenerator`.

| Method | Purpose |
|---|---|
| `addExpense(amount, category, description)` | Adds an expense dated today (overloaded) |
| `addExpense(amount, category, description, date)` | Adds an expense with a given date |
| `addRecurringExpense(...)` | Adds a recurring expense |
| `deleteExpense(id)` | Removes an expense or throws `ExpenseNotFoundException` |
| `getSortedByDate()`, `getSortedByAmount()` | Sorted lists using streams |
| `filterByCategory`, `filterByMonth`, `filterByAmountRange` | Filtered lists using streams and lambdas |
| `getTotalSpending()`, `getMonthlySpending(month)` | Totals |
| `getTotalsByCategory()` | Category-wise totals in an `EnumMap` |
| `setBudget(limit)` | Sets the monthly budget |
| `getRemainingBudget(month)` | Budget minus that month's spending |
| `checkBudget(month)` | Throws `BudgetExceededException` if over budget |
| `generateReport()` | Builds the text report |

### ui

**Main**: Shows the menu, reads and validates user input with a `Scanner`, calls `ExpenseManager`, and handles the exceptions by printing messages. It loads data at startup and saves it on exit.

## 6. Flow of Application

```
Start
  |
  v
Load expenses.txt and budget.txt  (start empty if files are missing)
  |
  v
Show menu <-------------------------------------+
  |                                             |
  v                                             |
Read choice                                     |
  |                                             |
  +--> 1/2  Read details, validate, add expense |
  |         Check budget, warn if exceeded -----+
  +--> 3    Sort and display expenses ----------+
  +--> 4    Delete by ID ------------------------+
  +--> 5    Filter and display -------------------+
  +--> 6    Show totals ---------------------------+
  +--> 7    Set monthly budget ---------------------+
  +--> 8    Show remaining budget -------------------+
  +--> 9    Print report ----------------------------+
  +--> 10   Save data -------------------------------+
  |
  +--> 0    Save data and exit
```

## 7. Data File Format

`expenses.txt` has one record per line with seven fields separated by `|`:

```
TYPE|id|amount|CATEGORY|description|date|intervalDays
EXPENSE|1|450.0|FOOD|Lunch at cafe|2026-10-31|0
RECURRING|3|799.0|ENTERTAINMENT|Netflix|2026-10-01|30
```

- `TYPE` is `EXPENSE` or `RECURRING`.
- `intervalDays` is `0` for a normal expense.
- A `|` typed in a description is replaced with `/` so it cannot break the format.

`budget.txt` holds one number, the monthly budget.

## 8. Exception Handling

- Invalid numbers and dates typed by the user are caught in `Main` (`NumberFormatException`, `DateTimeParseException`) and the user is asked again.
- Business rule errors use the custom exceptions listed in section 5.
- File errors (`IOException`) are caught when loading and saving. If loading fails, the program starts with empty data and shows a message.
- File reading and writing use try-with-resources.

## 9. Testing

`ExpenseManagerTest` has 12 JUnit 5 tests. Each test starts with a fresh `ExpenseManager` using `@BeforeEach`.

| Test | Checks |
|---|---|
| `addExpenseStoresExpense` | An added expense is stored |
| `addExpenseWithNegativeAmountThrows` | Negative amount throws `InvalidAmountException` |
| `addExpenseWithZeroAmountThrows` | Zero amount throws `InvalidAmountException` |
| `totalSpendingIsCorrect` | Total is the sum of all amounts |
| `deleteExpenseRemovesIt` | Deleted expense is gone |
| `deleteMissingExpenseThrows` | Unknown ID throws `ExpenseNotFoundException` |
| `filterByCategoryReturnsOnlyMatching` | Category filter works |
| `filterByAmountRangeWorks` | Amount range filter works |
| `remainingBudgetIsBudgetMinusMonthlySpending` | Remaining budget calculation |
| `expensesFromOtherMonthDoNotCountForCurrentMonth` | Old expenses are not counted this month |
| `checkBudgetThrowsWhenExceeded` | Exception thrown with the correct extra amount |
| `sortedByAmountIsDescending` | Highest amount comes first |

All 12 tests passed when the project was compiled and run with JDK 21 and JUnit 5.

## 10. Design Principles

**SOLID**

- Single Responsibility: models hold data, `ExpenseManager` holds logic, the store classes handle files, and `Main` handles input and output.
- Open/Closed: a new kind of expense can be added by extending `Expense` without changing `ExpenseManager`.
- Liskov Substitution: a `RecurringExpense` can be used anywhere an `Expense` is expected.
- Interface Segregation: `ReportGenerator` and `DataStore<T>` are small interfaces with only the methods needed.
- Dependency Inversion: storage is defined by the `DataStore<T>` interface, so the file storage can be replaced without touching the logic.

**Clean code**

- Meaningful class and method names, small methods, and one job per class.
- Duplicate amount checking is kept in one private method, `checkAmount`.
- No logic in model classes beyond simple calculations.
- Packages group related classes.

**Refactoring done**

- Amount validation was moved into a single `checkAmount` method used by `addExpense`, `addRecurringExpense` and `setBudget`.
- File reading and writing was separated from `ExpenseManager` into the `storage` package.

## 11. Limitations and Future Scope

- Recurring expenses are stored once and do not generate new entries by themselves.
- No option to edit an existing expense.
- If a saved line has a badly formatted number or date, loading fails and the program starts empty, so saving afterwards would overwrite the old file. Keep a copy of `expenses.txt` if you edit it by hand.
- Possible additions: income tracking, category-wise budgets, export to CSV, and a graphical interface using JavaFX or Swing.
