# Android Object LifeCycle / BackStack Flipper Plugin 

## What you get ?

This library is an app introspection plugin for the [Flipper](https://github.com/facebook/flipper) tool. It does focus on Android Sdk mains objects and their life cycle inspection.

It enables to see the **big internal picture** of your running app in a side tool. Meaning that :

 - trough running the app as a user
 - you can keep an eye on internals as a developer
 - see the dynamic of objects evolution
 - without struggling with debugger

## Status :

 - The plugin is **under development**
 - The plugin **name** is not sealed (ideas are welcome)
 - The **private Api** is not sealed at all (it will change in the future)
 - The **public Api** is simple : future breaking change will be easy to handle

## Availability :

You can get the Android plugin with the Gradle dependency from Jitpack :

- [https://jitpack.io/#afaucogney/flipper-backstack]()
- [![](https://jitpack.io/v/afaucogney/flipper-backstack.svg)](https://jitpack.io/#afaucogney/flipper-backstack)

Current version is `5.0.2`

## Vision

The idea behind this plugin is to enable understanding of what is happening during the usage of an Android Application by :

 - seeing logs about object life cycle changes
 - seeing internal Android object structure (limited to a set of specific architecture)
 - seeing objects stacks (FragmentManager BackStack, Jetpack Navigation Stack)
 - seeing current running objects
 - seeing objects destroyed (in Trash)

## Show me a ScreenShot !

![alt text](./screenshot/flipper_android_lifecyle_4.1.0.png)

### What is in the Left part ?

You can see the app object structure :

 - Application
 - Activities
 - Fragments
 - ViewModels (with reflexion)
 - ViewModel members (LiveDatas) (with reflexion)
 - Jobs (not yet available)
 - Services (not yet available)
 - Trash (objects destroyed)
 - FragmentManager BackStack (legacy, only for activity)
 - JetPack Navigation BackStack
 
The object structure is viewed as a tree, where each instance of an object has is dedicated object category.
 
 - `HomeFragment` parent is `fragments`
 - `HomeFragment` children are running instances of `HomeFragment`
    
### What is in the Right part ?

You can see the object lifeCycle events for :
  - activities
  - fragments

You can filter those event categories :
 - created / destroyed
 - started / stopped
 - paused / resumed
 - attached / detached
 - view-created / view-destroyed
 - SaveInstanceState     

## Installation

### Step 1 : Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

### Step 2 : Add the dependency

	dependencies {
		debugImplementation 'com.github.afaucogney:flipper-backstack:-SNAPSHOT'
	}
	
### Step 3 : Add Flipper to your app

Follow the [Getting Started](https://fbflipper.com/docs/getting-started/android-native) section of the Flipper documentation

### Step 4 : Add the Android-Object-LifeCycle plugin to Flipper

In your application, register the plugin the Flipper configuration.

    val client: FlipperClient = AndroidFlipperClient.getInstance(this)
    with(client) {
        addPlugin(
            InspectorFlipperPlugin(
                this@App,
                DescriptorMapping.withDefaults()
            )
        )
        ... 
        addPlugin(BackStackFlipperPlugin(this@App))
        start()
    }	
	
Be careful, to not embed this library in production !

### Step 5 : Add the Android-Object-LifeCycle plugin to Flipper-Desktop

Install the plgin in Flipper, it is called `lifecycle` [Npm page](https://www.npmjs.com/package/flipper-plugin-lifecycle)

## What can you achieve with the plugin ?
 
 - identify runtime object instances and structure
 - identify leaks (remaining objects)
 - identify bug instantiations (recreation Vs preservation)
 - understand architecture in a breeze
 - see object instantiation in real time
 - lifeCycle issues
 - backStack issues

## Consideration

- This plugin (as Flipper) must not be shipped in Production
- This plugin may doing lot of work, so please consider disabling it if you are not using it. I have not yet done measurement to evalute it resource requirement. But from the `5.0.0` version
 - It should run on Background tread thank to Rx
 - However, data structure of the app is kept in memory, so it may be a problem for some App or some low memory devices
- The Plugin uses Reflection for accessing privage properties or functions

## TODO

- [ ] Be able to filter object in structure
 - [ ] Impact event filtering from object
- [ ] Evaluate saving data in db / shareprefs
- [ ] Handle clean versioning / release
- [ ] Generate docs
- [ ] Impl Jobs support
- [ ] Impl Services support
- [ ] See bundle / saveinstance state
- [ ] See App lifecycle
- [ ] Evaluated StateFlow support (on going PR)
- [ ] Imp BlackStack for ChildFragmentManager
- [ ] Highligh duplicate object in the tree if you click on it (see link between framgnet in stack...)

## ISSUES

- [ ] At first start, the tree is not updated ! You need to change screen to update the plugin.

 

 
 