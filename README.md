A. Introduction

This project is about PhysVib: a software library extending a physics engine for
the automated vibrotactile feedback generation to collision events.
The current released project is implemented using the latest AndEngine 
(https://github.com/nicolasgramlich/AndEngine) and its Box2D extension 
(https://code.google.com/archive/p/andenginephysicsbox2dextension/source/default
/source).
This README document describes the structure of PhysVib, a demonstration 
application, and how to use it.



B. Structure

Basically PhysVib extends the Box2D to provide the vibrotactile feedback in the
form of the exponentially decaying sinusoid. It needs two external parameters
of vibration frequency and amplitude decay rate that are related to the material
property and the collision information respectively. In PhysVib, userdata.java
in the PhysVib.Data package contains them and each fixture in Box2D has to 
run setUserData() to assign this 'userdata' in its data structure. This userdata
contains a 'hapticCamera' boolean variable for the control of haptic enability.

PhysVib consists of three main components: Collision Catcher, Vibration Manager,
and Vibration Converter. The Collision Catcher receives collision information
from the simulation of the Box2D when a collision occurs, then calculates 
vibration parameters using the collision information and the userdata. These
parameters are sent to the Vibration Manager when an application sends a trigger
by calling setTrigger() in the CollisionCatcher.java.

The Vibration Manager receives vibration parameters from the Collision Catcher
and renders normalized vibration signals with the number of samples of 
SAMPLING_RATE / UPDATE_RATE on each update. Those two rate values are user
defined variables, and their default values are 5000 and 100 respectively.

The Vibration Converter receives the vibration signal generated by the Vibration
Manager using Handler in Java, then converts it to the appropriate form to a 
target system. At default, PhysVib provides two basic converters:
(1) CustomAndroidVibrationAPI.java and (2) CustomAudioOutput.java.
The former converts the signal to the on/off binary signal and actuates an  
embedded actuator of an android phone using the android default vibration 
function. The latter converts the signal to a audio signal and renders it using 
the audio channel, and usually voice-coil type actuators are capable of using
this type of converter.



C. A Demonstration Application

In the release, we added a project for a demonstration named "PhysVib Demonstration
and for Release". This demo application renders three different object pairs
with different frequencies and decay rates. By touching the 'focus' button, 
each pair is enabled to provide vibrotactile feedback to collisions by setting
the haptic camera variables of the fixtures of moving objects on. The 'Wall' or
'Objects' Button toggles targets of the haptic camera focus variables between
the wall and the objects. 

This application uses the CustomAudioOutput converter so the output vibration
signal is rendered via the audio channel. Also, this application is a good
example how to create the objects, set the userdata, initialize PhysVib, remove
the objects, and triggers the collision catcher.



D. How to Use

Initialization: 

1) Assign an instance of CollisionCatcher to the physicsworld using setContactListener();

2) Assign a handler in an instance of VibrationConverter to the VibrationManager using 
setCurrentVibrationAPI()

3) Set a pixel to meter ratio 

4) Set a pixel value of the longest axis on the screen 

5) Set true or false to the automatical control of normalization variables 

6) Set the minimum impulse value to be rendered

These initialization example is in the example.demonstration.ExampleDemonstration.java 
as a method with a name of "initializePhysVib()".

Object Creation: 

1) Create an object as ordinary AndEngine application does. 

2) Create a userdata instance and set the variables of frequency, decayrate, density, 
mass (if the object is static), and haptic camera variable. 

3) Assign the userdata instance to a fixture of the created object.

Vibration Trigger: 

1) Create a thread that regularly updates 

2) Call "setTrigger()" of the instance of CollisionCatcher 

3) If an object is added or removed in a scene, call "checkMass()" of the instance of
CollisionCatcher.

