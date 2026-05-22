# AGENTS.md

## Project Overview

`projmod` is a JavaFX desktop application called **BPMN Modeler**.

It is a visual BPMN-style editor that lets users place and connect elements such as:

- Start events
- Tasks
- Gateways
- Sequence flows
- Other BPMN-style diagram elements

The app supports:

- Canvas-based editing
- Save/load from a SQL database
- Undo
- Pluggable logging
- Optional log console window

The real application entry point is:

```java
app.MainApp

Do not use the template src/Main.java as the main entry point.

Architecture Rules

The application must stay organized using these layers:

Layer	Responsibility
Model	Holds domain objects such as Diagram, DiagramElement, and ElementType.
View	JavaFX UI classes only. Should not contain business logic.
Controller	Connects UI events to services.
Services	Business logic, diagram actions, persistence coordination, and logging coordination.
Persistence	DAO/database access only.

Keep the separation clean. Do not put SQL in controllers. Do not put UI logic in models. Humanity has suffered enough.

Allowed Design Patterns

Only the following design patterns should be used in this project:

Singleton
Factory Method
Abstract Factory
Adapter
Decorator
Observer
Strategy

Do not introduce other design patterns unless explicitly requested.

Especially avoid adding:

Command
DAO as a named pattern
Builder
Facade
Mediator
Repository
Service Locator
Event Bus

Existing logic may still exist, but when adding new code, prefer the allowed patterns above.

Required Comment Format

Whenever a class is using one of the allowed design patterns, add a short comment near the class declaration.

Use this format:

// Design Pattern: PatternName
// Purpose: Short explanation of why this pattern is used here.

Example:

// Design Pattern: Singleton
// Purpose: Provides one shared database connection manager for the application.
public class DBConnectionManager {
}

For methods that implement important pattern behavior, add a smaller comment:

// Factory Method: creates the correct BPMN element based on ElementType.

Do not over-comment obvious Java code. Comments should identify architecture decisions, not explain what if means. Java already punishes us enough.

Pattern Usage Guide
1. Singleton

Use Singleton only for application-wide shared services that must have one instance.

Allowed examples:

DBConnectionManager
Global logging coordinator, if needed
Shared configuration loader, if needed

Example comment:

// Design Pattern: Singleton
// Purpose: Ensures the application uses one shared database connection manager.

Avoid using Singleton for normal services, controllers, or views.

2. Factory Method

Use Factory Method when creating BPMN elements based on a type.

Allowed examples:

Creating a TaskElement
Creating a StartEventElement
Creating a GatewayElement
Creating a SequenceFlowElement

Example comment:

// Design Pattern: Factory Method
// Purpose: Creates BPMN diagram elements without exposing construction logic to controllers.

Recommended class:

BPMNElementFactory
3. Abstract Factory

Use Abstract Factory only when creating families of related BPMN objects.

Allowed examples:

A factory for BPMN node elements
A factory for BPMN flow elements
A factory for event elements, task elements, and gateway elements

Example comment:

// Design Pattern: Abstract Factory
// Purpose: Provides factories for related BPMN element families.

Use this only if the app needs multiple related factories. Do not force it where a simple Factory Method is enough.

4. Adapter

Use Adapter when wrapping external or incompatible APIs.

Allowed examples:

Adapting JDBC database results into Diagram objects
Adapting a file format into the internal model
Adapting logger output to the log console

Example comment:

// Design Pattern: Adapter
// Purpose: Converts database result data into application Diagram objects.

Do not use Adapter just to rename a method. That is not architecture, that is bureaucracy wearing a fake mustache.

5. Decorator

Use Decorator when adding behavior to an existing object without changing its core class.

Allowed examples:

Adding visual selection styling to a diagram element
Adding validation around a diagram element
Adding timestamp formatting around log messages

Example comment:

// Design Pattern: Decorator
// Purpose: Adds extra visual behavior to a diagram element without modifying the base element.

Do not subclass everything just to add one tiny behavior.

6. Observer

Use Observer for UI updates when the model changes.

Allowed examples:

ObservableList<DiagramElement>
Canvas redraw when diagram elements change
Log console updating when new logs arrive

Example comment:

// Design Pattern: Observer
// Purpose: Updates the canvas automatically when the diagram model changes.

JavaFX observable properties and observable lists are acceptable Observer implementations.

7. Strategy

Use Strategy when behavior can be swapped at runtime.

Allowed examples:

Logging to console
Logging to file
Logging to database
Logging to the UI console

Example comment:

// Design Pattern: Strategy
// Purpose: Allows switching the logging implementation at runtime.

Recommended classes:

AppLogger
ConsoleLogger
FileLogger
DatabaseLogger
LoggingService
Specific Refactoring Rules
Undo

The app currently uses command-style undo logic.

For this version, avoid naming or documenting it as the Command pattern.

Instead, treat undo as part of DiagramService.

Acceptable wording:

// Undo support: stores reversible diagram actions.

Do not write:

// Design Pattern: Command

Because Command is not in the allowed pattern list.

Persistence

Database persistence should remain isolated.

Acceptable class names:

DiagramDAO
DiagramDAOImpl
DBConnectionManager

However, do not document DAO as a design pattern.

Acceptable comment:

// Persistence: handles saving and loading diagrams from SQL database.

Not acceptable:

// Design Pattern: DAO

DAO is not part of the allowed pattern list.

Coding Rules
Keep controllers thin.
Keep views focused only on JavaFX UI.
Put business logic in services.
Do not access the database directly from views or controllers.
Do not create JavaFX UI elements inside model classes.
Use ElementType to decide what BPMN element should be created.
Use factories for object creation.
Use strategies for logging behavior.
Use JavaFX observables for UI refresh behavior.
Main Entry Point

Use this class to start the app:

app.MainApp

The app creates:

Diagram diagram = new Diagram("Untitled");
MainView mainView = new MainView(diagram);
MainController controller = new MainController(mainView, diagram);
Scene scene = new Scene(mainView, 1200, 800);

The primary window title is:

BPMN Modeler
Database Configuration

Database connection settings are loaded from:

db.properties

The file must be available on the classpath.

DBConnectionManager should manage database connection access.

Recommended comment:

// Design Pattern: Singleton
// Purpose: Centralizes SQL connection configuration and access.
Logging

Logging must use the Strategy pattern.

The app should support switching between logger implementations.

Possible logger strategies:

ConsoleLogger
FileLogger
DatabaseLogger
UILogger

Recommended interface:

AppLogger

Recommended comment:

// Design Pattern: Strategy
// Purpose: Defines interchangeable logging behaviors.

LoggingService should control the active logger strategy.

Final Rule

When modifying or generating code, use only these design patterns:

Singleton
Factory Method
Abstract Factory
Adapter
Decorator
Observer
Strategy

Every use of these patterns must be marked with a clear comment.

Do not introduce extra patterns unless the project owner explicitly asks for them.


Save it as:

```txt
AGENTS.md