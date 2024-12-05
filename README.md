# FX Wave Generation Simulator

FX Wave Generation Simulator is an educational Wave simulator that allows you to visualize waves through a graphical
interface. This app has the following educational purposes:

- Display the individual traveling waves 
- Show the combined wave & the given combined amplitude
- Hear the frequencies made from the interaction between waves
- Have an audio visualizer of the interactions
- Extrapolate given data from JSON files to facilitate the access
- Interactive graphical interface with a fully customized experienced

## Features

- Wave Generation logic (Math behind wave addition)
- Wave Simulation logic (the stepping and time system that 
graphs out each individual point depending on given parameters)
- Wave Display logic (How to correctly simulate the graph)
- Interactive graphical interface (UI Controls)
- A drawn chart that displays the waves using ChartFX
- Sound Interaction logic (The combined frequencies heard by the
interaction of waves)
- Sound visualizer logic (Visualizing the intensity of the sound 
with a pop-up)
- PUP windows that take user query (AddWaves)
- Database queries using SQLite 
- Ability to import & export files to local drives using JSON
- Read previous simulations using SQLite
- Presets that allow the user to facilitate many frequent shapes



## References

- [GSON User Guide](https://github.com/google/gson/blob/main/UserGuide.md#using-gson) - For JSON serialization and deserialization.
- https://egandunning.com/projects/timemanagement-timer.html - Fix for updating the JavaFX components from a non-JavaFX thread.
- https://docs.oracle.com/javafx/2/ui_controls/radio-button.htm - How to use RadioButtons in JavaFX.
- Generate Audio In-Real-Time based on Waves (frequency and amplitude):
    - Based on https://stackoverflow.com/questions/7782721/java-raw-audio-output/7782749#7782749 to generate audio with `Clip` class.
    - and used https://github.com/Cubik65536/BeeperFX for making a prototype that works with our Wave format.
- https://www.youtube.com/watch?v=NWcFTTbKbLs - Setting up the database and connecting to JavaFX
- https://github.com/fair-acc/chart-fx and sample codes - Setting up ChartFX, set axis, show data, etc.
- https://stackoverflow.com/a/13729491 - How to monitor the event when a CheckBox is checked/unchecked
- https://en.wikipedia.org/wiki/Hearing_range - Human hearing range
- https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/atomic/AtomicReference.html - Atomic References
- https://www.sqlite.org/docs.html for the Database and setup
- https://stackoverflow.com/questions/55253092/connecting-sql-server-with-java-javafx for Setting up connections
- https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/ListView.html List view and corresponding functions.
- 
