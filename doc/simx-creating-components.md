CSS: markdown/markdown5.css

[Back to Read Me](./simx-ReadMe.html)

![][simx-logo]
 
[simx-logo]: figures/simx-logo.png "simx-logo" width="4cm" align="right"


# Simulator X: Creating Components

## Table of Contents

* [Example components][]
	* [The component concept][]
	* [Creating new aspects for the component][]
	* [Defining data types and converters for the component][]
	* [Creating the new component][]
	* [The component class][]
		* [Internal Representation of Entities][]
		* [configure][]
		* [requestInitialValues][]
		* [entityConfigComplete][]
		* [performSimulationStep][]
		* [removeFromLocalRep][]

## Example components

The package component contains an example that shows how the implement a component and how to write create parameters that are processed by the component.  

## The component concept

Creating a new component requires some or all of the following steps: 

1. Create new aspects for the component
2. Define data types for the component
3. Create converters for the components data types
4. Creating the component itself

If a component shall be implemented to replace an existing one, the aspects which were provided with the first one should be reused to guarantee a seamless replacement process.
If a new type of component is created, aspects for this component must be defined. This will be described in the next section. 

The second and third step are related to each other: Most of the components that are attached to Simulator X do not use the same data types which are used in the rest of the system. To ensure the easy replacement of components the data types used by the new component and converters to the core representation of the application have to be defined. Both steps will be explained in the section "Defining data types and converters for the component" below.

The last---obviously mandatory and most time-consuming---step will be detailed after the previously mentioned three steps have been described. 

The example which will be used here creates an component that moves entities on a circular path. 

The main class `BasicComponentApplication is` essentially a simplified version of the `ExampleApplication` described above. Since the intention is to show how new components are defined it is reduced to the instantiation of the new `ExampleComponent`, which will be developed within the next sections. 

### Creating new aspects for the component

An aspect specifies the part of an entity which is visible to a specific component. For example, the aspect for a rendering component is likely to at least specify the transformation and the appearance (e.g., by providing a model file) of the entity.

All aspects have to be derived from the `EntityAspect` class. It is convenient to create a base class all of the aspects for a specific component are derived from. In our case this class is called `ExampleAspectBase`, shown in the next code snippet:

```scala
object ExampleComponent{
  val componentType = Symbols.exampleComponent
}

abstract class ExampleAspectBase( aspectType : GroundedSymbol, targets : List[Symbol] = Nil )
  extends EntityAspect(ExampleComponent.componentType, aspectType, targets)
```

Each component is identified by its `componentType` and `componentName`. By providing the component type and a list of targets (i.e., component names) the target component of an aspect can be specified. While providing a component type parameter is mandatory, the list of targets can be empty, resulting in all components of the given type receiving the aspect on creation of an entity. 

In the example shown above the first parameter given to the `EntityAspect` superclass is the example components component type. The type of the aspect is dependent on the respective derived aspect class, wherefore it is passed through from the deriving class. The list of targets defaults to be empty, but may be passed by a deriving class, too. 

In the example application developed here a ball known from the `ExampleApplication` above is used. To show all facets of an aspect definition, the example component also provides identifiers for entities. This results in the `ExampleSphereAspect` shown below:


```scala
case class ExampleSphereAspect(id : Symbol) extends ExampleAspectBase(Symbols.sphere){
  def getFeatures   = Set(Identifier, Transformation)

  def getProvidings = Set(Identifier)

  def getCreateParams = new NamedSValList(aspectType, Identifier(id))
}
```

The aspect defined here requires the created entity to contain state variables storing an identifier as well as the transformation of the entity. The component has to provide an initial value for the identifier state variable. To do so, a list of create parameters containing a symbol is provided. The list also contains the type of the aspect. 

Each class derived from the `EntityAspect` class has to implement three methods:

- `getFeatures`
- `getProvidings`
- `getCreateParams`

While `getFeatures` has to return a set of `SVarDescriptions` (which are defined in the ontology, i.e., `simx.core.ontology.types.Types`). The returned set specifies all state variables which the entity at least has to contain. This does not necessarily mean, that the state variables and initial values are provided by the component the example was designed for. 

The set returned by `getProvidings` is a subset of the `getFeatures` return value. The component must provide initial values for each `SVarDescriptions` which is contained in the returned set. 

The third method, `getCreateParams`, returns values which are used by the component to provide initial values for the state variables to be provided. In most cases, this means to simply apply `SVarDescriptions` with values that were passed to the constructor of the aspect. This is exemplified above, where a symbol is passed to the `SVarDescription` "Identifier", which will then be used by the new component (see below). 

A more complex use of the `getCreateParams` method would be to return a file from wich a component could load information which is used to provide initial values. As visible from that example, the return value of this method does not necessarily have to match the set retuned by the `getProvidings` method.


Similar to the examples described earlier in this document, the new aspect may now be used in `EntityDescription`s as shown below: 

```scala 
case class ExampleBall(name : String, radius : Float, position : ConstVec3) extends EntityDescription(
    // define information for the ExampleComponent
    ExampleSphereAspect(
      // assign an id for the ExampleComponent
      id             = Symbol("myId")
    ),
    // define information fot the renderer
    ShapeFromFile(
      file           = "assets/vis/ball.dae",
      scale          = ConstMat4(scale(radius * 0.5f)),
      transformation = Right(ConstMat4(translate(position)))
    )
  )
```

Besides our new `ExampleSphereAspect`, a `ShapeFromFile` aspect is included in order to provide the rendering component, which is used in this example, with information on how to visualize the entity. 

### Defining data types and converters for the component

Having defined an aspect, the new component will be involved in the creation process of entities whose descriptions contain the aspect. As mentioned above, a component will most likely use its own data types internally. Simulator X provides a mechanism to hide the conversion of all components internal data types to a global type. 
This section will show how to define internal data types and converters to use the automatic conversion mechanism (as shown in the next section).

Assume the new component does only support translation but no rotation, scaling or other aspects of the `Transformation` of an entity, since it internally uses 3-dimensional vectors to represent the transformation which is represented by matrices in the rest of the application (or may be by other data types, due to the automatic type conversion a component developer does not have to know). 

The following code example shows the definition of the internal data type (which usually is generated from the components OWL ontology file, but may be defined manually): 

```scala 
object LocalTrafo 
	extends SVarDescription[
		simplex3d.math.floatm.ConstVec3f, 
		simplex3d.math.floatm.ConstMat4f
	](
	simx.core.ontology.types.Transformation 
		as Symbols.transformation 
		createdBy simplex3d.math.floatm.Vec3f.Zero
	)
```

Whereas this looks quite complex, the definition is really simple: 
1. provide an scala object for the new type definition: `object Transformation`
2. let the object inherit from the `SVarDescription` class: `extends SVarDescription` 
3. specify the internal data type: `[simplex3d.math.floatm.ConstVec3f,`
4. specify the global data type: `simplex3d.math.floatm.ConstMat4f]`
5. specify the base class: `(simx.core.ontology.types.Transformation `
6. specify semantics for the internal type: `as Symbols.transformation`
7. specify a constructor for the internal data type: `createdBy simplex3d.math.floatm.Vec3f.Zero)`

Notes:
- step 3 and 4 are optional, however, some IDEs won't be able do infer those types correctly and will indicate errors in your (correct) code 
- step 6 is redundant in this specific case since the core definition of `Transformation` already has the same semantics definition. If the new data type would have another meaning, e.g., velocity, this has to be reflected at this point or else the state variables in an entity will be accessed incorrectly

To define converters from the internal data type to the global one, a new instance of the converter class has to be defined. To do so, two methods (`convert` and `revert`) have to be defined. In our case, the following example is a possible way to implement this:

```scala 
object ExampleConverters{
  def registerConverters(){
    new Converter(LocalTrafo)(Transformation){
      def convert(i: ConstVec3f) =
        ConstMat4f(Mat3x4f.translate(i))

      def revert(i: Transformation.dataType) =
        ConstVec3f(i.m03, i.m13, i.m23)
    }
  }
}
```

The converter is automatically registered and applied when necessary. To allow this, the types for which the converter can be applied are specified as constructor parameters: The first set of parameters (here: `LocalTrafo`) specifies the internal types for which it may be applied. In this case it is just one parameter, but an arbitrary number of types may be provided. The second parameter (here: `Transformation`) specifies the global type to which the internal type will be converted. 

The `convert` method transforms the internal type into the global one. In this example it creates a matrix that translates an object by the given 3-dimensional vector.

The `revert` method transforms the global type back to the internal type. In this example it simply reads the translation component of the given matrix and creates a new 3-dimensional vector.

### Creating the new component

A new component at least has to implement the `Component` trait, wherefore it is forced to implement seven methods: 

- `componentType`
- `componentName`
- `configure(params: SValList)`
- `requestInitialValues(toProvide: Set[ConvertibleTrait[_]], aspect: EntityAspect, e: Entity, given: SValList)`
- `entityConfigComplete(e: Entity, aspect: EntityAspect)`
- `performSimulationStep`
- `removeFromLocalRep(e: Entity)` 

The two methods `componentType` and `componentName` simply return the respective value. Actually, the example implementation shown below defines the name via a constructor parameter. Therefore only the `componentType` method is left to be implemented. Since the type of an component cannot change, this must not be set via constructor parameters.

The remaining five methods are a bit more complex and will be detailed below. For this reason, the code of the `ExampleComponent`, which is provided with Simulator X, will be explained line by line in the next sections.


#### The component class

As mentioned above, the `ExampleComponent` mixes the `Component` trait in. In the first section of the component, the `componentName` (as a constructor parameter) and the `componentType` are defined:

```scala 
class ExampleComponent(val componentName : Symbol) extends Component {
  ExampleConverters.registerConverters()

  def componentType = ExampleComponent.componentType
```

##### Internal Representation of Entities

Each component has its own representation of entities. A rendering component might, for example, store the required information in a scene graph, whereas a physics component might store completely different data in a differend kind of data structure (e.g., a list of all entities, oct-tree, etc.).

In this example, we create our own data structure: a simple case class, holding the identifier of the entity, its transformation state variable, the last known transformation, and a timestamp. 

The internal representations are stored in the maps `entitiesInCreation` and `simulatedEntities`, accessible via the respective entity's id. Linking the internal representations with the associated entity's id is a common thing to do, since all addressing of the entity will be done by means of its id. 

```scala 
  private case class LocalEntityRep(id : Identifier.dataType, posSVar : SVar[LocalTrafo.dataType], var lastPos : LocalTrafo.dataType, var lastUpdateTime : Long)

  private var entitiesInCreation = Map[java.util.UUID, LocalEntityRep]()
  private var simulatedEntities  = Map[java.util.UUID, LocalEntityRep]()
```


##### configure

The configure method is used to re-configure a component. In this example no configuration is necessary.

```scala
  protected def configure(params: SValList){}
}
```

##### requestInitialValues

As mentioned above, a component has to implement the `requestInitialValues method`. This method has four parameters: 

- `toProvide`: A set of `ConvertibleTrait`s which contains the types of initial values to be provided by the component. This is essentially the return value of the respective aspects `getProvidings` method. 
- `aspect`: the aspect which is currently processed. Using this parameter, the `getCreateParams` method of the aspect can be called to retrieve information that is required to provide the requested initial values.
- `e` the entity which is currently being created. Since the component could (and should) create its internal representation in the `requestInitialValues` method, the id of the entity can be used for later reference.
- `given`: a list of values which has already been provided by other components.

```scala 
  protected def requestInitialValues(toProvide: Set[ConvertibleTrait[_]], aspect: EntityAspect, e: Entity, given: SValList){
    val id    = aspect.getCreateParams.getFirstValueFor(Identifier)
    val trafo = given.getFirstValueFor(LocalTrafo)
    if (id.isDefined && trafo.isDefined)
      entitiesInCreation = entitiesInCreation.updated(e.id, LocalEntityRep(id.get, None, trafo.get,  -1))
    provideInitialValues(e, aspect.getCreateParams.combineWithValues(toProvide)._1)
  }
```

In the given example, the components creates its internal representation from the value for `Identifier`, which is taken directly from the aspect, and the initial value for `Transformation` (that is automatically converted into the internal data type by using the `SVarDescription` `LocalTrafo` to access it), taken from the list of values provided by other components. 

Note: In our application the rendering component provides the value for the `Transformation` state variable. Since the `ExampleComponent` requires the entity to contain such a variable, the renderer provides the value before the `ExampleComponent` is requested to provide initial values. 

The local representation is created and stored in the map `entitiesInCreation`. As it is not always the case that all dependencies of components relying on initial values provided by other components can be resolved by a single call to each involved component's `requestInitialValues` method, this mehtod might be called multiple times with different `toProvide` (and `given`) parameters. Therefore, it makes sense to store entities which have the status "in creation".

Finally the method `provideInitialValues` is called. This method has to provide all values requested in the `toProvide` set (an exception will be thrown otherwise). In this case we use a method of the set of create paramters: `combineWithValues`. It takes a set of `SVarDescriptions`, specifying the values to provide and returns a tuple, containing of the values that could be provided by means of the set of create parameters and a set of descriptions for values that could not be found. 

The call to that method is necessary, since the component could postpone loading of data or the processing of the initial values takes some time (e.g., a sub-actor has to react to be able to provide initial values). This implies that the invocation of `provideInitialValues` can occur at any given point in time but does not necessarily have to take place at the end of the `requrestInitialValues` method.


##### entityConfigComplete

The method `entityConfigComplete` is called each time all components involved in the creation process of an entity have provided all initial values and the entity is ready to be inserted into each components simulation loop. Its parameters are the entity `e` and the aspect which was used before, to allow the component to identify the type of the entity. This is the first time when an entities state variables can be accessed.

```scala  
  protected def entityConfigComplete(e: Entity , aspect: EntityAspect) {
    // retrieve local representation and remove it from the stored list
    val toSimulate      = entitiesInCreation(e.id)
    entitiesInCreation  = entitiesInCreation - e.id
    // store trafo svar
    toSimulate.posSVar  = e.get(LocalTrafo).headOption
    // add local rep to
    simulatedEntities  += e.id -> toSimulate
    // access the identifier
    val id = aspect.getCreateParams.getFirstValueForOrElse(Identifier)(throw new Exception("Did not find id"))
    println("Example component got entity with id \"" + id.name + "\"")
  }
```

In the given example the entity's id is used to retrieve the internal representation from the set of entities "in creation". The state variable (which is accessible now) is added to the internal representation (to avoid many lookup operations during the simulation) and the entity is added to the map of entities that are included in the simulation loop (see `performSimulationStep` below).
Finally a message, telling us that the entity was created, is printed on the command line.

##### performSimulationStep

After the entity has been created the component is meant to include it into its simulation loop. The method `performSimulationStep` is called on each iteration of the components simulation loop. How often this is the case depends on the used `ExecutionStrategy` which is defined in the application when the component is started.

```scala 
  protected def performSimulationStep() {
    val now = System.currentTimeMillis()
    simulatedEntities.values.foreach{ localRep =>
      if (localRep.lastUpdateTime < 0 )
        localRep.lastUpdateTime = now
      else {
        // time calculations
        val timeDiff            = (now - localRep.lastUpdateTime) / 16f
        localRep.lastUpdateTime = now
        // set new position
        localRep.lastPos        = (localRep.lastPos * Mat3x4f.rotateZ(FloatMath.radians(timeDiff))).xyz
        localRep.posSVar.collect{ case svar => svar.set( localRep.lastPos ) }
      }
    }
    simulationCompleted()
  }
```


In the presented example the object which is simulated shall be rotated. To allow a smooth rotation, the time between the last and the current iteration is measured (in case there was no previous iteration nothing happens). According to the amount of time that has passed a rotation matrix is calculated and multiplied with the old transformation of the entity.  

At the end of the `performSimulationStep` method `simulationCompleted` has to be called to inform dependent components to start their simulation or to calculate the time of the next invocation of `performSimulationStep`.

##### removeFromLocalRep

The last method to be implemented is `removeFromLocalRep`. This method informs the component, that an entity was removed from the simulation and has to be removed from the internal representation. The only parameter of this method is the entity which has to be removed. Usually, its id is used to identify the entity in the local representation.

In the `ExampleComponent` this simply means to remove the entity from all maps that may contain it:

```scala
  protected def removeFromLocalRep(e: Entity) {
    entitiesInCreation = entitiesInCreation - e.id
    simulatedEntities  = simulatedEntities  - e.id
  }
```

[Back to Read Me](./simx-ReadMe.html)