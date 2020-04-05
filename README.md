# Neurone Am Connector

Neurone-am-connector is part of the Neurone-AM (active monitoring) solution, compose of [neurone-am-coordinator](https://github.com/NEURONE-IL/neurone-am-coordinator.git) and [neurone-am-visualization](https://github.com/NEURONE-IL/neurone-am-visualization.git) also. All three components interact with each other to deliver real-time updated metrics over a persistent connection and provides tools for monitoring all users of [NEURONE](https://github.com/NEURONE-IL/neurone) search engine.

This component is a API REST that provides a set of services to calculate behavioral and performance metrics for participans of NEURONE search engine. Therefore, it interact directly with NEURONE database to get the all necesary data for the process. 


## Services

#### Definition
For each metric, the API provides two services. One to calculate the metric value for a specific participant and one for all participants. All services are listed below. For the specification of each metric, see the Neurone-am-visualization manual.

| Metric | One participant| All participants |
|--------|----------------|------------------|
| TotalCover | /totalcover/:username/ | /totalcover
| BmRelevant | /bmrelevant/:username | /bmrelevant
| ActiveBm | /activebm/:username | /activebm
| UsfCover | /usfcover/:username | /usfcover
| TotalPageStay | /pagestay/:username | /pagestay
| NumQueries | /numqueries/:username | /numqueries
| WritingTime | /writingtime/:username | /writingtime
| QueryEntropy| /entropy/:username | /entropy
| MouseClicks | /mouseclicks/:username | /mouseclicks
| MouseCoordinates | /mousecoordinates/:username | /mousecoordinates
| ScrollMoves | /scrollmoves/:username | /scrollmoves
| Precision | /precision/:username | /precision
| Recall | /recall/:username | /recall
| F1-Fscore | /f1/:username | /f1
| SearhScore | /score/:username | /score
| CoverageEffectiveness | /ceffectiveness/:username | /ceffectiveness
| QueryEffectiveness | /qeffectiveness/:username | /qeffectiveness

Aditionally, there are some optional query parameters availabes

* ti (seconds): Calculate the metric with data obtained ti seconds later from the start.
* tf (seconds): Calculate the metric with data obtained tf second before the current time.
* relevants: Quantity of relevants documents. It just work for recall and f1.
* limitTime: Limit time in seconds for UsfCover. It just work in usfcover, coverage effectiveness and query effectivess.

In addition to the metric services, there are two spefici service for especial purposes

* Multiples metric by user: Provide a set the selected metrics for a specific participant. Each metric is received as a query parameter. Also, all query parameters defined previously, can be used with this service.
    * example: /multiple/:username?metrics=totalcover&metric=precision.  

* Init stage time: The time at a participant enter to the searh engine.
   * example: /init/:username
 
#### Results

For all services described the result format is JSON. The structure of each type is described below:

 - Metrics Service One participant: The json include username, metric name and metric value as a field.
    - TotalCove and Precision
```JSON
{
    "username": "901ASCE110008",
    "totalcover": 6.0,
    "metric": "totalcover"
}
```
 ```JSON
 {
    "username": "901ASCE110008",
    "precision": 0.16666666666666666,
    "metric": "precision"
}
 ```
 
 - Metric Service for all participants: Include an array of json with the same fields as the presented before.
    - Recall example.

```JSON
[
  {
        "username": "901ASCE110003",
        "recall": 0.0,
        "metric": "recall"
    },
    {
        "username": "901ASCE110004",
        "recall": 1.0,
        "metric": "recall"
    },
    {
        "username": "901ASCE110005",
        "recall": 1.0,
        "metric": "recall"
    },
    {
        "username": "901ASCE110006",
        "recall": 0.3333333333333333,
        "metric": "recall"
    }
]
```
- Multiple metrics for one participant: The result is a json array with the requested metrics.
    - Example with Search Score and F1
```JSON
[
    {
        "username": "901ASCE110008",
        "score": 5.0,
        "metric": "score"
    },
    {
        "username": "901ASCE110008",
        "f1": 0.2222222222222222,
        "metric": "f1"
    }
]
```

- Init Stage Time: The result is a JSON with similar structure to the first case.
    - Example
```JSON
{
    "username": "901ASCE110008",
    "inittime": 1.485859108995E12,
    "metric": "inittime"
}
```

## Install Instructions
     
#### Development

1. Install Java JDK 8+.
2. Install [Scala](https://www.scala-lang.org/) 2.12+.
3. Install Sbt 1.2+..
4. Download/clone the code
5. Enter to de root directory of the project.
6. Set up cofigurations variables (ee configuration section below).
7. Run `sbt ~reStart` to init the API.
8. Each time the code change the app restart.

#### Production
The production enviroment is over docker, because that docker must be. The following instructions must be followed to deploy.

1. Download or clone the code.
1. Set up the configuration variables (see configuration section below)`.
2. Go to the root path of the project and run `./runDocker.sh`.
* In this script can be changed the port mapped from the docker container to the local host.

#### Build
To bundle the code sbt-native-package is used. Run the next instruccions:

1. In the project root run `sbt Stage`.
2. Go to `target/universal/stage/bin`.
3. Run `./neurone-conector` script to init the API.

#### Configuration
To Define the Mongo DB and Mongo URL (Production and Development) as well as the API port the application.conf file must be changed. It is located in `${project_root}/src/main/resources/application.conf`. This is a example of that file.

## License

Licensed under the  GNU Affero General Public License (Version 3) [LICENSE](LICENSE) ; you may not use this software except in compliance with the License.
