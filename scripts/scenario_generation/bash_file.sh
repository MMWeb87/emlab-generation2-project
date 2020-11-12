#!/bin/bash
MODEL="emlab-generation2.jar"
ITERATIONS=11
PARALLEL=4
#SCENARIO="DefaultScenario"
ROLE="EMlabModelRole"
REPORTER="DefaultReporter"
PRERUN="false"

SCENARIO="Mm00AuctionScenario"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm01BothAlltech10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm02BothOnshore10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm03BothOffshore10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm04BothPv10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm05DeAlltech10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm06DeOnshore10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm07DeOffshore10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm08DePv10Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm09BothAlltech20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm10BothOnshore20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm11BothOffshore20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm12BothPv20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm13DeAlltech20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm14DeOnshore20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm15DeOffshore20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
SCENARIO="Mm16DePv20Y"

  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER
