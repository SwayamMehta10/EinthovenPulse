# EinthovenPulse
Name: Swayam Atul Mehta

ASU ID: 1233120917

CSE 535 C Fall 2024 

Project 1 - Context Monitoring App

## 1) Imagine you are new to the programming world and not proficient enough in coding. But, you have a brilliant idea where you want to develop a context-sensing application like Project 1.  You come across the Heath-Dev paper and want it to build your application. Specify what Specifications you should provide to the Health-Dev framework to develop the code ideally.
 We can utilize the proposed Health-Dev framework to automatically generate downloadable code for our context-sensing application like Project 1 by providing it with information for the following three modules:

### A) Specifications

**a) Sensor Specifications:** This includes both the computation and communication requirements for each type of sensed signals i.e. analog signals from video for heart rate and electric signals from accelerometer for respiratory rate. We also maintain separate input and output ports for both the signals to sense the raw data and report it via a wireless network.

_i) Sensor Properties:_
|     |  Heart Rate Monitoring  | Respiratory Rate Monitoring |
|-----|--------|------|
| Sensor Type   | Camera with a flashlight | Accelerometer or Orientation Sensor |
| Sampling Frequency   |  30 frames per second for a period of 45 seconds  |   As provided by the Android accelerometer, typically in the range of 50-100 Hz |
| Sensitivity   | High enough to detect even subtle changes in light or color intensity as blood flows through the finger |    Adjust to capture even the smallest of chest movements during breathing as changes in accelerometer values |	
| Platform Type   | Android smartphone camera |    Android smartphone accelerometer |	

_ii) Sensor Subcomponents:_

For computation purpose, we have an algorithms subcomponent with input and output ports along with a property to specify the algorithm name. We then match the algorithm name with our maintained codebase of algorithms used for measuring the heart rate and respiratory rate for example, Photoplethysmography (PPG), Electrocardiography (ECG), Ballistocardiography (BCG), Capnography, Respiratory Inductance Plethysmography (RIP), Impedance Pneumography, etc. We also include the helper codes provided in our codebase. The Health-Dev framework also allows users to implement a custom function in the platform specific programming language and include it in the codebase. The heartRateCalculator function extracts frames from the recorded video, then processes the color data from the region of interest (red, green, blue values). It calculates the number of peaks corresponding to heartbeats. The respiratoryRateCalculator function processes the accelerometer’s x, y, and z values over 45 seconds, calculating respiratory rate by analyzing the changes in acceleration due to chest movements.

In the communication subcomponent, we need to specify the communication protocol. Bluetooth serves as the best option in our case for an Android app. Single-hop communication would be sufficient since all the sensors are integrated in the smartphone itself and there is not need for intermediary nodes or external devices. The data is processed and transferred directly on the same device. Even if we were to add an external sensor later (e.g., a Bluetooth pulse oximeter), single-hop Bluetooth communication between the smartphone and that device would still suffice. Multi-hop communication is typically used in distributed sensor networks where sensors are placed at different locations and cannot communicate directly with the base station. Data packets transmission would occur at the end of the 45 second measuring period. We cannot setup a recurring transmission at frequent intervals since in our case the user needs to perform some actions for the sensing to take place.

_iii) Sensor Connections:_

Health-Dev allows users to specify multiple inputs of each algorithm which can either be raw signals from multiple sensors or outputs from other algorithms. The communication subcomponent transmits the output to the smartphones. In our case, data processing and communication are all internal, happening directly on the smartphone.

**b) Network Specifications:** This subcomponent contains network topology with routing information, routing table, etc. and communication energy management schemes like always on, only on during transmission or duty cycled. Since, there is no external network of sensors involved in our project, no routing information is needed. The phone’s camera and accelerometer are active during the 45-second measurement period, after which they stop to save energy. The camera’s flashlight is also turned on only during heart rate measurement for better visibility.

**c) Smartphone Specifications:** For our application, we would mainly need the following UI components – PreviewView to display the camera preview, Start/Stop/Navigation Buttons and Text Views to display instructions and the measured values after calculation. OnClick functionality would also have to be implemented in the buttons to transfer control to the – in Start button to initiate the measurement and in Stop button to end the measurement and calculate the result.

### B) Parser Module 
Using the platform-independent specifications (AADL) as input, the sensor parser in the generates meta models for each platform which are then used by the Code Generation Module. The meta model includes information about sensor types, algorithms, and communication protocols. The smartphone parser reads the AADL specification for the user interface (UI) and converts it into the corresponding XML specification required for Android.

### C) Code Generation Module
The code generation module takes the output from the parser and generates the platform-specific code for both the sensor platform and the smartphone (Android). It relies on three types of codebases: sensor platform-specific, OS-specific, and smartphone-specific. Since the sensor platform in our project is a smartphone (Android), the generated code would follow an event-driven paradigm, where sensor input is handled in event listeners. The OS-specific codebase would contain algorithms for processing the physiological signals. The smartphone codebase would include parameterized declarations and callback functions for handling UI elements and data visualization on Android.

The authors have implemented Health-Dev using AADL as the specification language and OSATE to develop the parser and code generation modules. I propose the following version of the algorithm to accommodate both Android and sensor-based computations and processes:
```
1. Use OSATE’s component switch to parse the sensor components and identify which sensors (e.g., camera, flashlight, accelerometer) are used for heart rate and respiratory rate calculations.
2. For each sensor, group components based on sensor type (e.g., camera for heart rate, accelerometer for respiratory rate). Since these sensors are all internal to the smartphone, there is no need for multiple platforms.
3. Query the Android code repository for platform-specific base code. This will involve looking up the libraries needed for the camera, flashlight, and accelerometer (e.g., android.hardware.SensorManager for accelerometer and androidx.camera libraries for camera).
4. Based on the parsed sensor type, set the directive to "true". This directive will control which sensors are enabled in the code.
5. Since both the heart rate and respiratory rate measurements happen on the single smartphone, there is no need for external radio communication. Thus, set the communication protocol to internal processing (no external transmission required).
6. Use OSATE’s subcomponent switch to parse algorithm components (heartRateCalculator and respiratoryRateCalculator).
7. Query the Android-specific codebase for the algorithm definitions for both the heart rate and respiratory rate computations. Insert the algorithm definition into the main Android activity.
8. Ensure that algorithms are called in sequence if they depend on each other. In this case, heart rate and respiratory rate are independent, so their execution is not interdependent.
9. Ensure that function calls for each algorithm are correctly placed in the Android code and timer events are handled properly.
10. If an algorithm cannot be executed due to missing sensor data or a malfunction (e.g., camera or accelerometer not working), issue a semantic error and notify the user in the UI.
```

## 2) In Project 1 you have stored the user’s symptoms data in the local server. Using the bHealthy application suite how can you provide feedback to the user and develop a novel application to improve context sensing and use that to generate the model of the user?

Currently, my app EinthovenPulse tracks heart rate and respiratory rate but this could be extended in line with the bHealthy application suite in quite a few ways:

i) After each measurement session, the app can provide users with immediate feedback based on predefined ranges of normal or abnormal values. For instance, high heart rates or shortness of breath after minimal exertion could prompt a suggestion to engage in relaxation activities, further diagnostic assessments, using bHealthy’s training apps or consulting a healthcare provider.

ii) The physiological measurements can be combined with the symptom data (nausea, fever, etc.) to provide an overview of the user's health, much like bHealthy does with its wellness reports. For instance, if a user reports fever and has a high heart rate, the app can flag this as significant and offer advice. 

iii) bHealthy relies on sensors (like EEG, ECG) to assess users’ mental states and physical activity. We can also use the phone’s accelerometer and GPS to track movement throughout the day, providing more insight into activity levels, which can then be reflected in the heart and respiratory rate trends. We can also combine the physiological and environmental data using machine learning algorithms to model how different symptoms, heart rate, and respiratory rate change based on factors like time of day, physical activity, or stress.

iv) Similar to bHealthy's use of activity and environmental feedback, contextual notifications can be sent to users that guide them to remeasure their vitals in specific contexts (e.g., after exercise, in the morning, or when experiencing stress).

v) By generating periodic reports like bHealthy does, if certain symptoms, like fatigue or headaches, frequently coincide with abnormal heart or respiratory rates, the app can alert the user and suggest further action.

vi) Drawing on bHealthy’s use of physiological data to suggest mental state improvement apps, we can develop a system that suggests health interventions—such as breathing exercises, hydration reminders, or relaxation techniques—tailored to the user’s unique patterns.

## 3) A common assumption is mobile computing is mostly about app development. After completing Project 1 and reading both papers, have your views changed? If yes, what do you think mobile computing is about and why? If no, please explain why you still think mobile computing is mostly about app development, providing examples to support your viewpoint

Yes, my views on mobile computing have changed after completing project 1 and reading both the papers. Earlier, I used to think that mobile computing is just app development with importance given mainly to building interfaces, software functionality, handling input/output and optimizing performance. However, this project and the research papers have made me realize that mobile computing encompasses a much broader scope in real-world applications like health and wellness. Systems like Health-Dev emphasize model-based development, which involves creating high-level specifications and automatically generating code. This approach highlights the importance of system design and automation over manual app coding. Many mobile computing systems require real-time processing of data. This includes handling sensor data, processing inputs from various hardware components, and generating outputs instantaneously. For instance, applications like bHealthy use real-time physiological data for health monitoring and feedback, which involves complex data processing and immediate response mechanisms. It also involves integrating hardware components to create sophisticated context-aware systems that are also capable of interacting dynamically with users and their surroundings. I now understand the importance of managing cross-platform compatibility, handling sophisticated data analytics, communication protocols and ensuring seamless interoperability between devices and networks. These tasks require a deep understanding of both hardware and software, as well as the ability to integrate and adapt various technologies to meet specific user needs. Overall, this project has made it clear to me that mobile computing is about creating comprehensive, adaptive, and interconnected systems that leverage mobile technology's full potential, not just about building standalone applications.
