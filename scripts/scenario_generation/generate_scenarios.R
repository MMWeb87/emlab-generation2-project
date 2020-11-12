library(tidyverse)
library(glue)
library(readxl)
library(snakecase)

scenario_template_file <- "data/Scenario_NL_DE_intermittent_auction_pref_glue_template.java.txt"
scenario_data_file <- "data/scenarios.xlsx"
scenario_output_folder <- "/Users/marcmel/Development/java-projects/emlab-generation2-prj/emlab-source/src/emlab/gen/scenarios"

scenario_template <- read_file(file = scenario_template_file)
scenario_data <- read_xlsx(path = scenario_data_file, sheet = "scenarios", skip = 2)

glue_and_write <- function(single_scenario_data){
  
  single_scenario_data[["scenario_name"]] <- to_any_case(paste0("MM_", single_scenario_data[["scenario"]]), "upper_camel")
  filename = file.path(scenario_output_folder, paste0(single_scenario_data[["scenario_name"]], ".java"))
  
  glue_data(single_scenario_data, scenario_template) %>% 
    write_file(filename)
  
}

invisible(apply(scenario_data, 1, glue_and_write))
