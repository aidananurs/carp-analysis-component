# carp-analysis-component
 
[CARP Core](https://github.com/cph-cachet/carp.core-kotlin/tree/develop) is a software framework that helps developers create research platforms for conducting distributed data collection studies. This platform offers modules to define, deploy, and monitor research studies and to collect data from multiple devices at multiple locations. 
[The CARP platform](https://carp.cachet.dk/) gathers information on various health metrics of study participants, such as steps taken and heart rate. 

 **The CARP Analysis component** enables data analytics for the collected data. This component runs tasks for executing diverse scripts in Python (or other languages) to aggregate or analyze the data.

The analysis component can execute tasks of three types: 
- *Scheduled task*: the analysis is performed at a designated time, such as daily at midnight, for example.
- *Event-triggered task*: the analysis is performed upon the occurrence of a specific event.
- *One-Time task*: the analysis is performed in response to a request for analysis.

*SchedledTaskService, EventTaskService, OneTimeTaskService* classes expose endpoints to create|start|stop|retrieve tasks. 
Before creating a task, analysis scripts must be ready and run.

## Usage
The project needs to be incorporated as a library using Maven/Gradle similar to other [CARP Core components](https://github.com/cph-cachet/carp.core-kotlin/tree/develop).
This means that the project's code and dependencies will be packaged in a way that makes it easy for other projects to include and use its functionalities.

