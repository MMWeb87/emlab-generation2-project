library(tidyverse)
library(glue)
library(readxl)
library(snakecase)


scenario_data_file <- "data/scenarios.xlsx"
scenario_data <- read_xlsx(path = scenario_data_file, sheet = "scenarios", skip = 2)

get_scenario_name <- function(original_name){
  to_any_case(paste0("MM_", original_name), "upper_camel")
}

# Scenario files ----------------------------------------------------------

scenario_template_file <- "data/Scenario_NL_DE_intermittent_auction_pref_glue_template.java.txt"
scenario_template <- read_file(file = scenario_template_file)

scenario_output_folder <- "/Users/marcmel/Development/java-projects/emlab-generation2-prj/emlab-source/src/emlab/gen/scenarios"

glue_and_write <- function(single_scenario_data){
  
  single_scenario_data[["scenario_name"]] <- get_scenario_name(single_scenario_data[["scenario"]])
  filename = file.path(scenario_output_folder, paste0(single_scenario_data[["scenario_name"]], ".java"))
  
  glue_data(single_scenario_data, scenario_template) %>% 
    write_file(filename)
  
}

invisible(apply(scenario_data, 1, glue_and_write))

# Bash script -------------------------------------------------------------

bash_output_file <- "bash_file.sh"

bash_intro <- '#!/bin/bash
MODEL="emlab-generation2.jar"
ITERATIONS=11
PARALLEL=4
#SCENARIO="DefaultScenario"
ROLE="EMlabModelRole"
REPORTER="DefaultReporter"
PRERUN="false"\n\n'

write_file(x =bash_intro, path =  bash_output_file)



format_for_bash_run <- function(name){
  glue("SCENARIO=\"{get_scenario_name(name)}\"\n
  java -jar $MODEL prerun=PRERUN iterations=$ITERATIONS parallel=$PARALLEL scenario=$SCENARIO role=$ROLE reporter=$REPORTER\n\n") %>% 
    write_file(bash_output_file, append = T)
}

sapply(scenario_data$scenario, format_for_bash_run)



