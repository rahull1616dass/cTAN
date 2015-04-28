CSS: markdown/markdown5.css

[Back to Read Me](./simx-ReadMe.html)


![][simx-logo]

[simx-logo]: figures/simx-logo.png "simx-logo" width="4cm" align="right"

# Simulator X: Getting Started

## Table of Contents

* [Setting up the environment][]
	* [Git][]
	* [Java][]
	* [IntelliJ][]
* [Setting up the project][]	
* [Example applications][]
	* [Basic example][]
	* [Cluster example][]
* [Example components](./creating-components.html)	



**Note:**
*The first time after you compiled the application (which takes a while!), it may come up with an ClassNotFoundException. In most cases the reason for this ist the fact that IntelliJ did not recognize the class file fast enough after SBT created it. Simply start the application again.*

Simulator X is an evolutionary platform for software technology research in the area of intelligent RIS. The current architecture incorporates some well known abstractions and good practices as well as some interesting experimental features [latoschik:2011]:

- Unified concurrency model based on actors.
- Shared entity model using a distributed state.
- Component architecture using the concurrency model.
- Functional/OOP approach based on Scala.
- Semantic binding for the integration of AI.
- Generic support for multimodal interfaces.

## Setting up the environment
This section describes how to setup a system to obtain, built and run Simulator X to ultimately develop own applications. The Simulator X project is hosted at [GitHub](https://github.com) and mainly developed using the programming language [Scala](http://www.scala-lang.org) in conjuction with the IDE [IntelliJ](http://www.jetbrains.com/idea/). To compile and run the Scala code an installation of the Java Development Kit (JDK) is required. Considering all dependancies the supported operating systems are (at least) Windows (7 and 8), Linux and MacOSX (10.6 and later).

### Git
The Simulator X project is hosted at [GitHub](https://github.com) and can be accessed via the following project URL:

[https://github.com/organizations/simulator-x](https://github.com/organizations/simulator-x)

To obtain the source code you will likely need to setup the version control system (VCS) git on your system. See for example GitHub's [Getting Started](https://help.github.com/articles/set-up-git) for help.

### Java
Simulator X runs on the Java Virtual Machine (JVM). It is compatible with Oracles's Java 6 on all supported operating systems. In addition it is compatible with Oracles's Java 7 on Windows systems.

To compile and run Simulator X you need the Java Development Kit (JDK) to be installed on you system and your _path_ environment to be configured to contain your JDK's _bin_ folder (_E.g. you should be able to execute `java` and `javac` in a terminal if your configuration is right._).

### IntelliJ
The repository of Simulator X obtained from GitHub contains a preconfigured IntelliJ project as a easy starting point for the development own applications. In order to use this configuration you need to install at least the free community version of JetBrain's IDE [IntelliJ IDEA](http://www.jetbrains.com/idea/index.html) as well as the plugins _SBT_ and _scala_ (see JetBrain's [plugin page](http://www.jetbrains.com/idea/plugins/index.html) for a how-to).

## Setting up the project

First you need to download the project files. Go to [Simulator X's project page](https://github.com/organizations/simulator-x) and obtain the files via one of GitHub's download alternatives. Store them at a nice place at your system (_Hint: Choosing a path containing no whitespaces or special characters prevents errors in some cases. In general any valid path should be fine._)

The files you downloaded contain a IntelliJ project. Open IntelliJ, select _Open Project_ and choose the root folder of the files you just downloaded. The Simulator X project should load as a result, presenting you [IntelliJ's default project view](#intellij-default).

![IntelliJ's default project view][intellij-default]

[intellij-default]: figures/intellij-default.png "intellij-default" width="100%"

You may need to add your Java SDK in the project settings menu (_File → Project Structure → SDKs_) and set this SDK as project SDK (_Project Settings → Project_). 

## Example applications

The Simulator X project contains three demo applications to showcase the framework and to provide a starting point for development: A basic example, component examples and GenerateBasicExamplesSymbols, which generates Symbols from an online OWL ontology. To compile and execute these example applications simply select and run one of the preconfigured [configurations](#run-cfgs) from the IntelliJ project (e.g. by clicking on the green "play button").

![Run configurations][run-cfgs]

[run-cfgs]: figures/run-cfgs.png "run-cfgs"

On the left side of [IntelliJ's user interface](#intellij-default) you see all modules the Simulator X IntelliJ project consist of. Besides basic functionality (like in _core_) or component implementations (like in _jbullet_ for physics or _jvr_ for graphics), the _basicexamples_ module contains the before mentioned fundamental examples for Simulator X. 

### Basic example
This section describes the application contained in the _ExampleApplication_ file (loctated in the package ) in detail. To compile and run the example, simply select and apply the corresponding [run configuration](#run-cfgs). The application demonstrates the basics of Simulator X's API in the context of a simple example that combines object creation and manipulation as well as input handling while using graphics and physics functionality. The resulting virtual scene shows [a ball jumping on a rotating table](#basic-example-render) in the render window on startup. By pressing the `space` key one can spawn additional balls above the table. The position of the mouse inside the focused render output window is used to manipulate the viewing direction of the virtual camera, allowing a slight "look around". In addition to the rendering output the application opens another window, containing a [graphical editor](#basic-example-edit) for the simulation's world state (that will be described later in this section). 

![Rendering output of the basic example application.][basic-example-render]

[basic-example-render]: figures/simx-be-render.png "basic-example-render" width="100%"

![Graphical world state editor.][basic-example-edit]

[basic-example-edit]: figures/simx-be-edit.png "basic-example-edit" width="100%"

#### Starting Up

In the following the implementation of this example will be explained in detail. The source code of the example is located in the module _basicexamples_ in the package `simx.applications.basicexamples`. The first important lines are the those:

```scala
class ExampleApplication extends SimXApplication with JVRInit {
  /*...*/
}
```

The starting point of the basic example applications is the class `ExampleApplication`, which extends the `SimXApplication` trait. This trait is provided to support the rapid implementation of simple applications like this example. It realizes one potential way to setup and run a Simulator X application. For this purpose it deals with common initialization and shutdown procedures internally and defines three interface methods for component- and content creation as well as for additional application logic. In addition to the `SimXApplication` trait, the second mixed in trait (`JVRInit`) initializes _OpenGL_ for the utilized rendering component. It is of secondary importance for this example.

#### Creating Components

The first thing to do to fulfill the  `SimXApplication` trait is to create the components that are required for the application. In this case there are a rendering component, a physics component and an editor. Simulator X already comes with several pre implemented components for typical tasks. These implementations are located in the `simx.components` package. The example uses the rendering component `JVRConnector` based on the open source java renderer [jVR](http://cga200.beuth-hochschule.de/jVR/) implemented at the Beuth Hochschule für Technik Berlin, the physics component `JBulletComponent` based on the open source physics engine [JBullet](http://jbullet.advel.cz) and the editor component `Editor` implemented directly for Simulator X: 

```scala
val gfxName = Symbol("renderer")
val physicsName = Symbol("physics")
val editorName = Symbol("editor")

protected def createComponents() {
  println("[Creating components]")

  create(new JBulletComponent(physicsName))
  create(new JVRConnector(gfxName))
  create(new Editor(editorName))
}
```

The method `createComponents` shown here implements the respective method defined in `SimXApplication`. It is the designated location for component creation. The method `create` is then used to create all desired components, by passing the component's constructor (see the respective component documentation for details concerning alternative constructor parameters). The passed component name is a standard argument for components that can be used to reference the component later. It is extracted into a local value here to avoid error resulting of typos in component names. 

The use of the method `create` instead of a direct constructor call is necessary, inter alia, to guarantee a transparent interface for local as well as clustered applications. In the latter case it could be necessary to instantiate a component on a different machine. Without a direct constructor call a switch between a local and a distributed version of the application can be configured without changing the actual application code.

One important consequence of this decoupling in terms of component creation is that the `create` method has no return value. As stated before, the creation of a component could lead to its instantiation on another machine. Following the paradigms of the [actor model][actor] that underlies Simulator X, this could (in the case of a distributed application) happen asynchronously using message passing.
To provide a convenient way of creating component and to give a example for advanced users (in the source code), the `SimXApplication` trait carries out every component creation request (using `create`) inside `createComponents` in parallel and then waits, until every component's creation process is finished, before calling `configureComponents`.          

[actor]: http://dl.acm.org/citation.cfm?id=1624804

#### Configuring Components

Most components need a separate configuration to run properly. This configuration can be arbitrarily altered (again) at runtime, in contrast to the component's constructor arguments. Besides these configurations the execution scheme as well as inter component synchronization and miscellaneous application logic involving components may be configured.

This example configures the rendering component to use a 800 x 600 px render window, to run with _low_ shadow quality and no (_none_) mirror effects, to run at a target frame rate of 60 fps and to close the application if the render window is closed. The physics component is configured to apply gravity along the negative y-axis (`ConstVec3f(0, -9.81f, 0)`) and to run at a target frame rate of 60 fps as well. The configuration for the editor component simply sets the application name (used for example in the [graphical user interface](#basic-example-edit)). Since the editor is a passive component, that solely reacts to value changes it does not need an `ExecutionStrategy`.

```scala
protected def configureComponents(components: immutable.Map[Symbol, Component]) {
  println("[Configuring components]")

  components(gfxName) ! 
    ConfigureRenderer(
      BasicDisplayConfiguration(800, 600), 
      EffectsConfiguration("low","none")
    )
  start(ExecutionStrategy where components(gfxName) runs Soft(60))
  exitOnClose(components(gfxName), shutdown _) // register for exit on close

  components(physicsName) ! PhysicsConfiguration (ConstVec3f(0, -9.81f, 0))
  start(ExecutionStrategy where components(physicsName) runs Soft(60))

  components(editorName) ! EditorConfiguration(appName = "MasterControlProgram")
}
```

The described configuration is realized by implementing the `configureComponents` defined in the `SimXApplication` trait. This method is called after all component creation processes requested in `createComponents` are completely finished (see [Creating Components][]). The results of the creation processes are available via `configureComponents`' parameter `components`. As a second consequence of the underlying [actor model][actor], the created components are actors themselves and results are references to their actor interface.

To access single component references the component name is utilized. The actual configuration is then transferred to the component, using the [actor model][actor]'s message passing operator `!` (see the component's documentation for details on valid configuration messages).

The execution scheme and the inter-component synchronization is configured using the `start` method and the `ExecutionStrategy` domain specific language (DSL from the `simx.core.components` package (see the respective documentation for details).

The method `exitOnClose` provided by the `JVRInit` trait is used to logically connect the closing of the renderer window with the call of the `shutdown` method (from the `SimXApplication` trait) (This is a specific functionality of the utilized rendering component `JVRConnector` realized directly via message passing).

#### Creating Entities

After the successful creation and configuration of all desired components the objects of the simulation, so called _entities_, can be created. Besides visible objects other items, like lights, virtual cameras or input devices are considered to be entities in Simulator X. The creation of such entities is divided into two conceptional parts: The description of an entity defining all its desired properties and the realization of such an entity description (e.g. resulting in actual rendering or simulation). The description of entities itself is achieved by specifying a set of aspects for an entity, each intended to be realized by a specific component type.  

In the presented example application, three entities are created at startup: a table, a ball and a light. The ball has graphical, physical and naming aspects, that state the following (The descriptions of table and light are similar and not described here):

- Ball is an graphical object loaded from a (collada-) file with a specific scaling
- Ball is a physical sphere with a specific radius and position
- Ball is is named _the ball_

After the entities have been realized a respective command line output is generated.

```scala
private var tableEntityOption: Option[Entity] = None
private val ballRadius = 0.2f
private val ballPosition = ConstVec3f(0f, 1.5f, -7f)

protected def createEntities() {
  println("[Creating entities]")

  val ballDescription =
    EntityDescription (
      PhysSphere(
        restitution    = 0.998f,
        transform      = ballPosition,
        radius         = ballRadius
      ),
      ShapeFromFile(
        file           = "assets/vis/ball.dae",
        scale          = ConstMat4f(Mat3x4f.scale(ballRadius*2f)),
        transformation = ReadFromElseWhere
      ),
      NameIt("the ball")
    )

  ballDescription.realize(entityComplete)
  Light("the light", Vec3f(-4f, 8f, -7f), 270f, -25f, 0f).realize(entityComplete)
  Table("the table", Vec3f(3f, 1f, 2f), Vec3f(0f, -1.5f, -7f)).realize((tableEntity: Entity) => {
    entityComplete(tableEntity)
    tableEntityOption = Some(tableEntity)
  })
}

private def entityComplete(e: Entity) {println("[Completetd entity] " + e)}
```

The described entity creation is implemented in `createEntities`, an interface method defined in `SimXApplication`. It is called directly after `configureComponents` finished. 
The description of the ball (stored in `ballDescription`) is created using the `EntityDescription` class. `EntityDescription`'s constructor directly realizes the concept of describing entities as set for aspects, by accepting an arbitrary number of `EntityAspect` objects. In the case of the ball description the physics aspect `PhysSphere`, the graphics aspect `ShapeFromFile` and the naming aspect `NameIt` are used (for details concerning available aspect implementations or their constructor parameters, see the respective documentation or the respective comments in the `simx.core.components`). These aspects are directly related to the functionality provided by the created components (e.g. `JVRConnector` or `JBulletComponent`).

In order to create a ball entity from the `ballDescription` the method `realize` (from the `EntityDescription` class) is called. This call brings the components to realize the respective aspects and Simulator X to actually create the entity. Because components, Simulator X core functionality as well the example application itself are all implemented on-top of the actor [actor model][actor] model and hence are realized by utilizing or rather being actors, this is achieved by message passing. Similar to the case of component creation, one consequence is that the result of `realize` is not immediately available. Thus, `realize` takes one function as parameter that handles the described entity once its realization is completed. In the example of the `ballDescription` the function `entityComplete` (a function that takes one entity and prints _[Completetd entity] ..._ to the console) is passed to `realize`.

Similar to the ball, the light and the table are realized. Their `EntityDescription`s are sourced out to the classes `Light` and `Table`. By doing so these `EntityDescription` can easily be reused, while varying properties can be set via the constructor (e.g. the position). In addition to printing out a console message, the function passed to the `realize`-call on the table description stores the completed entity in a class variable for later use.

The results of the three calls of `realize` in of components and output are:

- The ball and the table collada files are loaded by the renderer and displayed in the [render output window](#basic-example-render)
- The light is created by the renderer and the scene is lid accordingly
- The physical shapes of the ball and the table are loaded by the physics component and simulated accordingly (the table is a static object - mass = 0 - that is not affected by gravity)
- Due to the combination of physical and graphical aspects in one `EntityDescription`, the position and orientation of the ball (and the table) calculated by the physics component's simulation are shared with the rendering component. This results in the display of the ball jumping on the table in the render output window.  
- The created entities are listed in the [editor GUI](#basic-example-edit-2) according to their `NameIt` aspect 

Note: The order in which the realized entity descriptions are completed (resulting in the actual entity) is not predetermined (due to the underlying [actor model][actor] and the implementation of the process in Simulator X). Related messages are sent and processed asynchronously and in parallel.

#### Setting up additional application logic

Now that all initially required entities are created, the application window shows a ball jumping on a table. In this state, the application does not provide any possibility of interaction or manipulation by the user (except of using the editor) yet. In order to implement such _application logic_, the `SimXApplication` trait defines one additional method that is call right after entity creation and is designated for the realization of any additional requirement. Note that this approach of realizing _application logic_ is only suitable for small and simple applications. If things become more complex other approaches, e.g.\ like creating own dedicated components, should be used (see the documentation dealing with the creation of components).

In the presented example application three additional behaviors are implemented:

- The continuous animation (i.e. rotation) of the table.
- The spawning of new balls as reaction to a key press.
- A simple camera look around using the mouse.

These examples showcase the following basic operations:

- Access of entity properties, so called _state variables_
- Manipulation of _state variables_
- Scheduling of jobs
- Reaction to the change of _state variables_
- Creation and removal of entities in general
- Access of input device data (here keyboard and mouse) 

```scala
protected def finishConfiguration() {
  println("[Configuring application]")
  rotateTable()
  initializeBallSpawning()
  initializeMouseControl()
  println("[Application is running] Press SPACE to spawn new balls!")
}
```

The described behaviors are setup inside `finishConfiguration`, an interface method provided by the `SimXApplication` trait. `finishConfiguration` is called right after `createEntities` from within the `SimXApplication` trait. For this example application the implementations of the described behaviors are sourced out into the methods `rotateTable`, `initializeBallSpawning` and `initializeMouseControl`.

```scala
private def rotateTable() {
  addJobIn(16L, () => {
    tableEntityOption.collect{ case tableEntity => {
      val transformationSVar = tableEntity.get(types.Transformation).head
      transformationSVar.get(
        (currentTransform) => {transformationSVar.set(rotate(currentTransform))})
    }}
    rotateTable()
  })
}

private def rotate(mat: ConstMat4f) = mat * ConstMat4f(Mat3x4f.rotateY(0.01f))
```

The fist method, `rotateTable`, implements the continuous animation (i.e. rotation) of the table. Animations like this typically require a kind of loop, that continuously updates the object's properties (e.g. its orientation) dependent of the time that passed since the last update. Here this loop is realized by utilizing a recursive approach as well as the job scheduling method `addJobIn` provided by the trait `SVarActor` via `SimXApplication`.

A call of `rotateTable` does only one thing: It schedules a job, that is to be carried out in 16 milliseconds. To do so two parameters are passed `addJobIn` to `addJobIn`: 

- The `Long` "16", defining when the job is to be carried out and 
- an anonymous `Function0[Unit]` (or using another Scala syntax `() => Unit`), a function that takes no parameters and has no return value, defining the job itself.

When the scheduled job is finally carried out after 16 milliseconds (see the documentation of the underlying `SVarActor` implementation for details), the passed anonymous function is called. This function then applies two steps: An update of the table's orientation and the scheduling of the next update. 

In order to update the table's orientation the member variable `tableEntityOption` (see [Creating Entities][]) is checked. If it is defined, i.e. the realization of the table has already been completed, the _state variable_ containing the table entity's position and orientation is retrieved. This is done by passing the respective identifier for the property of interest (`types.Transformation`) to the `get` method of the `tableEntity`. Since the entity could contain more than one property of the type `Transformation`, `get` returns a collection of _state variables_. However this example implicitly requires that there is only one `Transformation`. Thus the first resulting  _state variable_ is selected via the method `head` (_In more complex applications it is reasonable to check if the list is not empty, rather than just call `head`_). This single result (`transformationSVar`) is an instance of the class `SVar`, that can be used to retrieve and manipulate this entity property or to react to its changes (see the documentation of the _state variable_ concept for details). Here a combination of retrieval and subsequent manipulation is used: First the current value of the table's position and orientation (a transformation matrix) is retrieved via the method `get`. Since this retrieval may require the exchange of messages between underlying actors, its result cannot be processed directly. Instead a function has to be passed, defining what to to with the value (in the example `currentTransform`) once it is available (see the documentation of the _state variable_ concept for details). Then, this function uses `currentTransform` to apply a very simple update operation (sourced out to `rotate`) and to manipulate the table's transformation _state variable_ `transformationSVar` via its method `set` accordingly.

After this update of the table's orientation (in fact after the request of the current transformation and the definition of what to do with it when its available via `get`) the method `rotateTable` is called in order to schedule the next update in 16 milliseconds. The overall result is that the orientation of the table is altered every 16 milliseconds. Since these updates are passed to the respective _state variable_ of the table's entity, components that are responsible for the realization of an aspect (see Creating Entities][]) of this entity can react to this changes. In the context of this example the previously created rendering component can update its internal representation (e.g. its scenegraph) to render the rotated table. Note that the connection between the table entity and the rendering component is established because of the inclusion of the `ShapeFromFile` aspect into the entity's `EntityDescription`.

```scala
private def initializeBallSpawning() {
  handleDevice(types.Keyboard)( (keyboardEntity) => {
    val spaceSVar = keyboardEntity.get(types.Key_Space).head
    spaceSVar.observe( (pressed) => {if(pressed) spawnBall()} )
  })
}

private var ballCounter = 0

private def spawnBall() {
  ballCounter += 1
  val randomOffset = Vec3f(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) * 0.05f
  Ball("ball#" + ballCounter, ballRadius, ballPosition + randomOffset).realize( (newBallEntity) => {
    val transformationSVar = newBallEntity.get(types.Transformation).head
    transformationSVar.observe( (newTransform) => {
      if(extractHeight(newTransform) < -2f) newBallEntity.remove()
    })
  })
}

private def extractHeight(mat: ConstMat4f) = mat.m13
```

The second method, `initializeBallSpawning`, implements the spawning of new balls as reaction to a key press. Two main steps have to be taken here:

- Access of the data coming from the keyboard, i.e. the keyboard's current state
- and the definition of a reaction to the change of this state.

Data coming from input devices, like a keyboard or a mouse, are represented in the same manner as other objects of the simulation: They are represented by entities. In the case of the keyboard the corresponding representation is an entity containing _state variables_ for every keyboard key. An entity representing an input device usually has not to be created from within an application. This is typically done by an component that is able to provide such data. In this case the rendering component can tab key presses and mouse movements inside the rendering window and provide it to the application via entities. To grant other software parts (like the this application code) access to this entities, they are registered globally (see the _worldinterface_ documentation). 

The look-up of registered entities is carried out by the method `handleDevice` provided by the `IORegistryHandling` trait via `SimXApplication`. By passing the identifier `types.Keyboard` the respective entity is requested (in the same manner as the access of entity properties). The underlying mechanism carries out the necessary message passing and takes care of temporal dependancies: If for example the keyboard entity is requested is before the rendering component finished its creation, the response is simple postponed until its available. Similar to the access of _state variables_ values, the result of this request cannot be used directly. Instead `handleDevice` requires a function, as a parameter, that defines what to to with the result.

Here this function uses the result of the request, the `keyboardEntity`, to retrieve its _state variable_ `spaceSVar` representing the space key (see the description of `rotateTable` in the previous paragraph for details). The _state variable_'s method `observe` is then used to register a reaction to value changes. This is done by passing a function that takes this value a parameter and defines what to in consequence of a value change. In the case of the `spaceSVar` this function takes one `Boolean` parameter `pressed`. The function body then checks if `pressed` is `true`. This means, the space key state changed and is now is pressed. In this case the method `spawnBall` is called.

`spawnBall` carries out the actual reaction to the key press: It creates a new ball entity and handles it removal. To create a new ball the sourced out `EntityDescription` `Ball` is used (see [Creating Entities][]). In order to prevent the balls from spawning at the exact same position a slight `randomOffset` is utilized. Furthermore the ball's names are appended by a consecutively numbering.

After the creation of the new ball entity is completed a reaction to changes of the balls position is registered. This is done by passing a respective anonymous function to `realize`. Using the same mechanisms as for the observation of the space key, a reaction the the changes of the ball's position (contained in its `transformationSVar`) is defined: If the ball's new hight falls below a defined value it is to be removed. In the context of the example the method `extractHeight` extracts the hight of the ball from its transformation matrix, using the table's 3D-model orientation to derive that the y-axis is pointing up. The threshold value `-2f` is used to determine balls that fell slightly below the table's top. If a ball's position is determined to be "too low", the method `remove` is called on the corresponding entity. This call leads to the notification of all involved components as well as to the removal of all _state variable_ data. In this example the rendering-, physics-, naming- and editor component will remove the ball from their internal representation.

```scala
private var userEntityOption: Option[Entity] = None

private def initializeMouseControl() {
  handleDevice(types.User)( (userEntity) => userEntityOption = Some(userEntity))
  handleDevice(types.Mouse)( (mouseEntity) => {
    val positionSVar = mouseEntity.get(types.Position2D).head
    positionSVar.observe{ case newMousePosition => {
      userEntityOption.collect{ case userEntity =>
       val viewplattformTransformationSvar = userEntity.get(types.ViewPlatform).head
       viewplattformTransformationSvar.set(calculateView(newMousePosition))
      }
    }}
  })
}

private def calculateView(mousePos: ConstVec2f) = {
  val weight = 0.1f
  val angleHorizontal = ((mousePos.x - 400f) / -400f) * weight
  val angleVertical = ((mousePos.y - 300f) / -300f) * weight
  ConstMat4f(Mat3x4f.rotateY(angleHorizontal).rotateX(angleVertical))
}
```

The third method, `initializeMouseControl`, implements a simple camera look around using the mouse. It uses the same approach and mechanisms as the two previous behavior implementations in order to react to mouse movement by manipulating the virtual camera's orientation.

Like the `keyboardEntity` before the entity `userEntity`, representing the virtual camera (TODO: Why is this retrieved via handeleDEVICE?), and the entity `mouseEntity`, representing data coming from the mouse, are requested by utilizing the method `handleDevice`. The following is done with the requested entities, when they are available.

`userEntity` is stored in the local variable `userEntityOption` for later use. `mouseEntity` is utilized to access the _state variable_ `positionSVar`, representing the mouse's current position inside the rendering window, via the identifier `types.Position2D`. By applying `positionSVar`'s `observe` method a reaction to a change of the mouse position is then registered:

If `userEntityOption` is defined, i.e. the request for `userEntity` has already been answered, its _state variable_, representing the virtual camera's position and orientation, is accessed via `types.ViewPlatform` and is then `set` to a new orientation depending on the `newMousePosition`. The calculation of the new orientation is sourced out to `calculateView`, a pragmatic implementation that is of no further relevance. The result of this "reaction-definition" is, that the camera view slightly rotates in the direction of the mouse, if it is moved from the center of the window to the border.
  
#### Starting the application

Now, all initially required entities are now created and additional _application logic_ is set up. To actually start the application the `SimXApplicationMain` trait is used. It provides a convenient interface to start Simulator X applications and requires the constructor of one actor to be initially started. In case of the presented example this actor is `ExampleApplication`. Since `ExampleApplication` derives from `SimXApplication` it is a `SVarActor`, implemented to setup the application as described above at startup.

```scala
object ExampleApplication extends SimXApplicationMain( new ExampleApplication )
```

The `SimXApplicationMain` trait solely contains a `main`-Method and is equal to the following written out object:

```scala
object ExampleApplicationMain {
  def main(args : Array[String]){
    SVarActor.createActor(new ExampleApplication)
  }
}
```

When running the application two windows should appear as described in the initial paragraph of the [Basic example][]: A rendering output window as well as a [graphical editor](#basic-example-edit).


#### Using the Editor

The editor is designed to provide a graphical user interface for the manipulation of the world state during runtime. All created entities (see previous sections) are displayed in a tree view on [the left side of the editor window](#basic-example-edit-2). 

![Entities displayed by the graphical world state editor.][basic-example-edit-2]

[basic-example-edit-2]: figures/simx-be-edit-2.png "basic-example-edit-2" width="100%"

By expanding entities in the tree view, a list of all their _state variables_ can be accessed. The names displayed for these _state variables_ correspond to the identifiers used in the code. If a specific _state variable_ is selected in the tree view, two corresponding views become visible on [the right side of the editor window](#basic-example-edit-3): 

- One view, on the top, to set the value of this state variable
- and one view, on the bottom, that displays its current value.

![Viewing and setting _state variable_ values using the graphical world state editor.][basic-example-edit-3]

[basic-example-edit-3]: figures/simx-be-edit-3.png "basic-example-edit-3" width="100%"

These views are dependent on the _state variable_'s type and semantics. If the top view is used to alter the _state variable_'s current value, the new value is passed directly to the running application. This is realized by using `SVar`'s method `set`, just like in the application's implementation.

### Cluster example

The cluster example showcases a distributed system setup where Simulator X is used to transparently handle bootstrapping, communication and synchronization...

The package cluster contains several examples using the clustering sub system of Simulator X.

The package syncgroup contains an example that shows how the sync groups in Simulator X work.

[Back to Read Me](./simx-ReadMe.html)