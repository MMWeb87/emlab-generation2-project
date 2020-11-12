/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emlab.gen.scenarios;

import emlab.gen.domain.agent.BigBank;
import emlab.gen.engine.Scenario;
import emlab.gen.domain.agent.EMLabModel;
import emlab.gen.domain.agent.EnergyConsumer;
import emlab.gen.domain.agent.EnergyProducer;
import emlab.gen.domain.agent.Government;
import emlab.gen.domain.agent.NationalGovernment;
import emlab.gen.domain.agent.Regulator;
import emlab.gen.domain.factory.FuelFactory;
import emlab.gen.domain.factory.LDCFactory;
import emlab.gen.domain.factory.PowerPlantCSVFactory;
import emlab.gen.domain.gis.Zone;
import emlab.gen.domain.market.electricity.ElectricitySpotMarket;
import emlab.gen.domain.market.electricity.SegmentLoad;
import emlab.gen.domain.policy.EmpiricalMappingFunctionParameter;
import emlab.gen.domain.policy.renewablesupport.BiasFactor;
import emlab.gen.domain.policy.renewablesupport.RenewablePotentialLimit;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportFipScheme;
import emlab.gen.domain.policy.renewablesupport.RenewableSupportSchemeTender;
import emlab.gen.domain.policy.renewablesupport.RenewableTarget;
import emlab.gen.domain.technology.Interconnector;
import emlab.gen.domain.technology.IntermittentResourceProfile;
import emlab.gen.domain.technology.PowerGeneratingTechnology;
import emlab.gen.domain.technology.PowerGridNode;
import emlab.gen.domain.technology.PowerPlant;
import emlab.gen.domain.technology.Substance;
import emlab.gen.engine.Schedule;
import emlab.gen.repository.Reps;
import emlab.gen.role.investment.InvestInPowerGenerationTechnologiesWithTenderAndPreferencesRole;
import emlab.gen.role.investment.InvestInPowerGenerationTechnologiesWithTenderRole;
import emlab.gen.trend.GeometricTrend;
import emlab.gen.trend.StepTrend;
import emlab.gen.trend.TimeSeriesCSVReader;
import emlab.gen.trend.TriangularTrend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author ejlchappin
 * @author marcmel
 * Last RUN: 1597914377374
 */
public class Scenario_NL_DE_intermittent_auction_pref implements Scenario {

	private String name;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void build(Schedule schedule) {
		
		Reps reps = schedule.reps;

		//Main simulation
		reps.emlabModel = new EMLabModel();
		reps.emlabModel.setExitSimulationAfterSimulationLength(true);
		reps.emlabModel.setSimulationLength(35); // input number of ticks to run
		reps.emlabModel.setDeletionAge(5);
		reps.emlabModel.setDeletionOldPPDPBidsAndCashFlowsEnabled(true);//speed up simulation by deleting old objects
		reps.emlabModel.setCapDeviationCriterion(0.03);
		reps.emlabModel.setIterationSpeedCriterion(0.005);
		reps.emlabModel.setIterationSpeedFactor(3);
		reps.emlabModel.setRealRenewableDataImplemented(true);
		reps.emlabModel.setCo2TradingImplemented(true);
		reps.emlabModel.setLongTermContractsImplemented(false);
		reps.emlabModel.setDismantleBehaviour("Lifetime"); // or LOSS
		

		reps.emlabModel.setName("EMLab Model");

		//Demand
		TriangularTrend demandGrowthTrendNL = new TriangularTrend();
		demandGrowthTrendNL.setTop(1.02);
		demandGrowthTrendNL.setMax(1.03);
		demandGrowthTrendNL.setMin(0.98);
		demandGrowthTrendNL.setStart(1.00);


		TriangularTrend demandGrowthTrendDE = new TriangularTrend();
		demandGrowthTrendDE.setTop(1.00);
		demandGrowthTrendDE.setMax(1.05);
		demandGrowthTrendDE.setMin(0.99);
		demandGrowthTrendDE.setStart(1.00);


		FuelFactory fuelFactory = new FuelFactory(reps);
		Substance biomass = fuelFactory.createFuel("biomass",0,25000,0.5,new TriangularTrend(1.01,1.05,.97,112.5));
		Substance uranium = fuelFactory.createFuel("uranium",0,3.8e9,1,new TriangularTrend(1.01,1.02,1,5000000));
		Substance fuelOil = fuelFactory.createFuel("fueloil",7.5,11600,1,new TriangularTrend(1.01,1.04,.96,2.5));
		Substance hardCoal = fuelFactory.createFuel("hardcoal",2.784,29000,1,new TriangularTrend(1,1.04,.97,60));
		Substance ligniteCoal = fuelFactory.createFuel("lignitecoal",0.41,3600,1,new TriangularTrend(1,1.02,.98,22));
		Substance naturalGas = fuelFactory.createFuel("naturalgas",0.00187,36,1,new TriangularTrend(1.01,1.06,.95,.32));

		Substance electricity = new Substance();
		electricity.setName("electricity");
		electricity.setCo2Density(0);
		electricity.setEnergyDensity(0);
		electricity.setQuality(1);
		reps.substances.add(electricity);

		Substance co2 = new Substance();
		co2.setName("CO2");
		co2.setCo2Density(1);
		co2.setEnergyDensity(0);
		co2.setQuality(1);
		reps.substances.add(co2);

		Zone nl = new Zone();
		nl.setName("nl");
		reps.zones.add(nl);

		Zone de = new Zone();
		de.setName("de");
		reps.zones.add(de);

		PowerGridNode nlNode = new PowerGridNode();
		nlNode.setName("nlNode");
		nlNode.setCapacityMultiplicationFactor(1.0);
		nlNode.setZone(nl);
		reps.powerGridNodes.add(nlNode);

		PowerGridNode deNode = new PowerGridNode();
		deNode.setName("deNode");
		deNode.setCapacityMultiplicationFactor(1.0);
		deNode.setZone(de);
		reps.powerGridNodes.add(deNode);

		Interconnector interconnectorNetherlandsGermany = new Interconnector();
		
//		GeometricTrend interconnectorNetherlandsGermanyTimeSeries = new GeometricTrend();
//		interconnectorNetherlandsGermanyTimeSeries.setStart(4450);
		
		interconnectorNetherlandsGermany.setCapacity(4450);
		//interconnectorNetherlandsGermany.setCapacity(0); // only TEST

		Set<PowerGridNode> connections = new HashSet<>();
		connections.add(nlNode);
		connections.add(deNode);
		interconnectorNetherlandsGermany.setConnections(connections);
		reps.interconnector = interconnectorNetherlandsGermany;


		// Load 
		TimeSeriesCSVReader lcReaderNL = new TimeSeriesCSVReader();
		lcReaderNL.setFilename("/data/ldcNLDE-hourly.csv");
		lcReaderNL.setDelimiter(",");
		lcReaderNL.readCSVVariable("NL");
		nlNode.setHourlyDemand(lcReaderNL);

		TimeSeriesCSVReader lcReaderDE = new TimeSeriesCSVReader();
		lcReaderDE.setFilename("/data/ldcNLDE-hourly.csv");
		lcReaderDE.setDelimiter(",");
		lcReaderDE.readCSVVariable("DE");
		deNode.setHourlyDemand(lcReaderDE);



		//  Intermittent Production Profiles NL
		IntermittentResourceProfile windProfileOffShoreNL = new IntermittentResourceProfile();
		windProfileOffShoreNL.setFilename("/data/renewablesNinja2015Profiles.csv");
		windProfileOffShoreNL.setDelimiter(",");
		windProfileOffShoreNL.readCSVVariable("OFF_NL");
		windProfileOffShoreNL.setIntermittentProductionNode(nlNode);
		reps.intermittentResourceProfiles.add(windProfileOffShoreNL);

		IntermittentResourceProfile windProfileOnShoreNL = new IntermittentResourceProfile();
		windProfileOnShoreNL.setFilename("/data/renewablesNinja2015Profiles.csv");
		windProfileOnShoreNL.setDelimiter(",");
		windProfileOnShoreNL.readCSVVariable("ON_NL");
		windProfileOnShoreNL.setIntermittentProductionNode(nlNode);
		reps.intermittentResourceProfiles.add(windProfileOnShoreNL);

		IntermittentResourceProfile solarProfileNL = new IntermittentResourceProfile();
		solarProfileNL.setFilename("/data/renewablesNinja2015Profiles.csv");
		solarProfileNL.setDelimiter(",");
		solarProfileNL.readCSVVariable("PV_NL");
		solarProfileNL.setIntermittentProductionNode(nlNode);
		reps.intermittentResourceProfiles.add(solarProfileNL);

		//  Intermittent Production Profiles DE
		IntermittentResourceProfile windProfileOffShoreDE = new IntermittentResourceProfile();
		windProfileOffShoreDE.setFilename("/data/renewablesNinja2015Profiles.csv");
		windProfileOffShoreDE.setDelimiter(",");
		windProfileOffShoreDE.readCSVVariable("OFF_DE");
		windProfileOffShoreDE.setIntermittentProductionNode(deNode);
		reps.intermittentResourceProfiles.add(windProfileOffShoreDE);

		IntermittentResourceProfile windProfileOnShoreDE = new IntermittentResourceProfile();
		windProfileOnShoreDE.setFilename("/data/renewablesNinja2015Profiles.csv");
		windProfileOnShoreDE.setDelimiter(",");
		windProfileOnShoreDE.readCSVVariable("ON_DE");
		windProfileOnShoreDE.setIntermittentProductionNode(deNode);
		reps.intermittentResourceProfiles.add(windProfileOnShoreDE);

		IntermittentResourceProfile solarProfileDE = new IntermittentResourceProfile();
		solarProfileDE.setFilename("/data/renewablesNinja2015Profiles.csv");
		solarProfileDE.setDelimiter(",");
		solarProfileDE.readCSVVariable("PV_DE");
		solarProfileDE.setIntermittentProductionNode(deNode);
		reps.intermittentResourceProfiles.add(solarProfileDE);




		// Load duration curve
		LDCFactory ldcFactory = new LDCFactory(reps);
		TimeSeriesCSVReader ldcReader = new TimeSeriesCSVReader();
		ldcReader.setFilename("/data/ldcNLDE-20segments.csv");
		ldcReader.setDelimiter(",");

		ldcReader.readCSVVariable("lengthInHours");
		ldcFactory.createSegments(ldcReader.getTimeSeries());


		ldcReader.readCSVVariable("loadNL");
		Set<SegmentLoad> loadDurationCurveNL = ldcFactory.createLDC(ldcReader.getTimeSeries());

		ldcReader.readCSVVariable("loadDE");
		Set<SegmentLoad> loadDurationCurveDE = ldcFactory.createLDC(ldcReader.getTimeSeries());


		ElectricitySpotMarket netherlandsElectricitySpotMarket = reps.createElectricitySpotMarket("DutchMarket", 2000, 40, false, electricity, demandGrowthTrendNL, loadDurationCurveNL, nl);
		ElectricitySpotMarket germanyElectricitySpotMarket = reps.createElectricitySpotMarket("GermanMarket", 2000, 40, false, electricity, demandGrowthTrendDE, loadDurationCurveDE, de);

		reps.createCO2Auction("CO2Auction", 0, true, co2);      

		EnergyConsumer energyConsumer = new EnergyConsumer();
		energyConsumer.setName("EnergyConsumer");
		energyConsumer.setContractWillingnessToPayFactor(1.2);
		energyConsumer.setContractDurationPreferenceFactor(.03);
		energyConsumer.setLtcMaximumCoverageFraction(0.8);
		reps.energyConsumers.add(energyConsumer);


		// ——————————————————————————————————————————————————
		// Carbon prices 
		// ——————————————————————————————————————————————————


		reps.bigBank = new BigBank();

		StepTrend co2TaxTrend = new StepTrend();
		co2TaxTrend.setDuration(1);
		co2TaxTrend.setStart(5);
		co2TaxTrend.setMinValue(5);
		co2TaxTrend.setIncrement(0);

		StepTrend co2CapTrend = new StepTrend();
		co2CapTrend.setDuration(1);
		co2CapTrend.setStart(10e9);
		co2CapTrend.setMinValue(0);
		co2CapTrend.setIncrement(0);
		
		StepTrend minCo2PriceTrend = new StepTrend();
		minCo2PriceTrend.setDuration(1);
		minCo2PriceTrend.setStart(7);
		minCo2PriceTrend.setMinValue(0);
		minCo2PriceTrend.setIncrement(1.5);

		
		reps.government = new Government();
		reps.government.setName("EuropeanGov");
		reps.government.setCo2Penalty(500);
		reps.government.setCo2TaxTrend(co2TaxTrend);
		reps.government.setCo2CapTrend(co2CapTrend);
		reps.government.setMinCo2PriceTrend(minCo2PriceTrend);
		

		StepTrend minCo2PriceTrendNL = new StepTrend();
		minCo2PriceTrendNL.setDuration(1);
		minCo2PriceTrendNL.setStart(0);
		minCo2PriceTrendNL.setMinValue(0);
		minCo2PriceTrendNL.setIncrement(0);

		StepTrend minCo2PriceTrendDE = new StepTrend();
		minCo2PriceTrendDE.setDuration(1);
		minCo2PriceTrendDE.setStart(0);
		minCo2PriceTrendDE.setMinValue(0);
		minCo2PriceTrendDE.setIncrement(0);

		NationalGovernment governmentNL = reps.createNationalGovernment("DutchGov", nl, minCo2PriceTrendNL);  
		NationalGovernment governmentDE = reps.createNationalGovernment("GermanGov", de, minCo2PriceTrendDE);    

		// ——————————————————————————————————————————————————
		// Technologies
		// ——————————————————————————————————————————————————
		
		

		GeometricTrend coalPSCInvestmentCostTimeSeries = new GeometricTrend();
		coalPSCInvestmentCostTimeSeries.setStart(1365530);

		GeometricTrend coalPSCFixedOperatingCostTimeSeries = new GeometricTrend();
		coalPSCFixedOperatingCostTimeSeries.setStart(40970);

		GeometricTrend coalPSCEfficiencyTimeSeries = new GeometricTrend();
		coalPSCEfficiencyTimeSeries.setStart(.44);

		PowerGeneratingTechnology coalPSC = reps.createPowerGeneratingTechnology();
		coalPSC.setName("Coal PSC");
		coalPSC.setCapacity(750);
		coalPSC.setIntermittent(false);
		coalPSC.setApplicableForLongTermContract(true);
		coalPSC.setPeakSegmentDependentAvailability(1);
		coalPSC.setBaseSegmentDependentAvailability(1);
		coalPSC.setMaximumInstalledCapacityFractionPerAgent(0);
		coalPSC.setMaximumInstalledCapacityFractionInCountry(0);
		coalPSC.setMinimumFuelQuality(.95);
		coalPSC.setExpectedPermittime(1);
		coalPSC.setExpectedLeadtime(4);
		coalPSC.setExpectedLifetime(40);
		coalPSC.setFixedOperatingCostModifierAfterLifetime(.05);
		coalPSC.setMinimumRunningHours(5000);
		coalPSC.setDepreciationTime(20);
		coalPSC.setEfficiencyTimeSeries(coalPSCEfficiencyTimeSeries);
		coalPSC.setFixedOperatingCostTimeSeries(coalPSCFixedOperatingCostTimeSeries);
		coalPSC.setInvestmentCostTimeSeries(coalPSCInvestmentCostTimeSeries);
		Set<Substance> coalPSCFuels = new HashSet<>();
		coalPSCFuels.add(hardCoal);
		coalPSC.setFuels(coalPSCFuels);

		GeometricTrend lignitePSCInvestmentCostTimeSeries = new GeometricTrend();
		lignitePSCInvestmentCostTimeSeries.setStart(1700000);

		GeometricTrend lignitePSCFixedOperatingCostTimeSeries = new GeometricTrend();
		lignitePSCFixedOperatingCostTimeSeries.setStart(41545);

		GeometricTrend lignitePSCEfficiencyTimeSeries = new GeometricTrend();
		lignitePSCEfficiencyTimeSeries.setStart(.45);

		PowerGeneratingTechnology lignitePSC = reps.createPowerGeneratingTechnology();
		lignitePSC.setName("Lignite PSC");
		lignitePSC.setCapacity(1000);
		lignitePSC.setIntermittent(false);
		lignitePSC.setApplicableForLongTermContract(true);
		lignitePSC.setPeakSegmentDependentAvailability(1);
		lignitePSC.setBaseSegmentDependentAvailability(1);
		lignitePSC.setMaximumInstalledCapacityFractionPerAgent(0);
		lignitePSC.setMaximumInstalledCapacityFractionInCountry(0);
		lignitePSC.setMinimumFuelQuality(.95);
		lignitePSC.setExpectedPermittime(1);
		lignitePSC.setExpectedLeadtime(5);
		lignitePSC.setExpectedLifetime(40);
		lignitePSC.setFixedOperatingCostModifierAfterLifetime(.05);
		lignitePSC.setMinimumRunningHours(5000);
		lignitePSC.setDepreciationTime(20);
		lignitePSC.setEfficiencyTimeSeries(lignitePSCEfficiencyTimeSeries);
		lignitePSC.setFixedOperatingCostTimeSeries(lignitePSCFixedOperatingCostTimeSeries);
		lignitePSC.setInvestmentCostTimeSeries(lignitePSCInvestmentCostTimeSeries);
		Set<Substance> lignitePSCFuels = new HashSet<>();
		lignitePSCFuels.add(ligniteCoal);
		lignitePSC.setFuels(lignitePSCFuels);

		GeometricTrend biomassCHPInvestmentCostTimeSeries = new GeometricTrend();
		biomassCHPInvestmentCostTimeSeries.setStart(1703320);

		GeometricTrend biomassCHPFixedOperatingCostTimeSeries = new GeometricTrend();
		biomassCHPFixedOperatingCostTimeSeries.setStart(59620);

		GeometricTrend biomassCHPEfficiencyTimeSeries = new GeometricTrend();
		biomassCHPEfficiencyTimeSeries.setStart(.35);

		PowerGeneratingTechnology biomassCHP = reps.createPowerGeneratingTechnology();
		biomassCHP.setName("Biomass CHP");
		biomassCHP.setCapacity(500);
		biomassCHP.setIntermittent(false);
		biomassCHP.setApplicableForLongTermContract(true);
		biomassCHP.setPeakSegmentDependentAvailability(0.7);
		biomassCHP.setBaseSegmentDependentAvailability(0.7);
		biomassCHP.setMaximumInstalledCapacityFractionPerAgent(1);
		biomassCHP.setMaximumInstalledCapacityFractionInCountry(1);
		biomassCHP.setMinimumFuelQuality(0.5);
		biomassCHP.setExpectedPermittime(1);
		biomassCHP.setExpectedLeadtime(3);
		biomassCHP.setExpectedLifetime(30);
		biomassCHP.setFixedOperatingCostModifierAfterLifetime(.05);
		biomassCHP.setMinimumRunningHours(0);
		biomassCHP.setDepreciationTime(15);
		biomassCHP.setEfficiencyTimeSeries(biomassCHPEfficiencyTimeSeries);
		biomassCHP.setFixedOperatingCostTimeSeries(biomassCHPFixedOperatingCostTimeSeries);
		biomassCHP.setInvestmentCostTimeSeries(biomassCHPInvestmentCostTimeSeries);
		Set<Substance> biomassCHPFuels = new HashSet<>();
		biomassCHPFuels.add(biomass);
		biomassCHP.setFuels(biomassCHPFuels);

		GeometricTrend ccgtInvestmentCostTimeSeries = new GeometricTrend();
		ccgtInvestmentCostTimeSeries.setStart(646830);

		GeometricTrend ccgtFixedOperatingCostTimeSeries = new GeometricTrend();
		ccgtFixedOperatingCostTimeSeries.setStart(29470);

		GeometricTrend ccgtEfficiencyTimeSeries = new GeometricTrend();
		ccgtEfficiencyTimeSeries.setStart(.59);

		PowerGeneratingTechnology ccgt = reps.createPowerGeneratingTechnology();
		ccgt.setName("CCGT");
		ccgt.setCapacity(775);
		ccgt.setIntermittent(false);
		ccgt.setApplicableForLongTermContract(true);
		ccgt.setPeakSegmentDependentAvailability(1);
		ccgt.setBaseSegmentDependentAvailability(1);
		ccgt.setMaximumInstalledCapacityFractionPerAgent(1);
		ccgt.setMaximumInstalledCapacityFractionInCountry(1);
		ccgt.setMinimumFuelQuality(1);
		ccgt.setExpectedPermittime(1);
		ccgt.setExpectedLeadtime(2);
		ccgt.setExpectedLifetime(30);
		ccgt.setFixedOperatingCostModifierAfterLifetime(.05);
		ccgt.setMinimumRunningHours(0);
		ccgt.setDepreciationTime(15);
		ccgt.setEfficiencyTimeSeries(ccgtEfficiencyTimeSeries);
		ccgt.setFixedOperatingCostTimeSeries(ccgtFixedOperatingCostTimeSeries);
		ccgt.setInvestmentCostTimeSeries(ccgtInvestmentCostTimeSeries);
		Set<Substance> ccgtFuels = new HashSet<>();
		ccgtFuels.add(naturalGas);
		ccgt.setFuels(ccgtFuels);

		GeometricTrend ocgtInvestmentCostTimeSeries = new GeometricTrend();
		ocgtInvestmentCostTimeSeries.setStart(359350);

		GeometricTrend ocgtFixedOperatingCostTimeSeries = new GeometricTrend();
		ocgtFixedOperatingCostTimeSeries.setStart(14370);

		GeometricTrend ocgtEfficiencyTimeSeries = new GeometricTrend();
		ocgtEfficiencyTimeSeries.setStart(.38);

		PowerGeneratingTechnology ocgt = reps.createPowerGeneratingTechnology();
		ocgt.setName("OCGT");
		ocgt.setCapacity(150);
		ocgt.setIntermittent(false);
		ocgt.setApplicableForLongTermContract(true);
		ocgt.setPeakSegmentDependentAvailability(1);
		ocgt.setBaseSegmentDependentAvailability(1);
		ocgt.setMaximumInstalledCapacityFractionPerAgent(1);
		ocgt.setMaximumInstalledCapacityFractionInCountry(1);
		ocgt.setMinimumFuelQuality(1);
		ocgt.setExpectedPermittime(0);
		ocgt.setExpectedLeadtime(1);
		ocgt.setExpectedLifetime(30);
		ocgt.setFixedOperatingCostModifierAfterLifetime(.05);
		ocgt.setMinimumRunningHours(0);
		ocgt.setDepreciationTime(15);
		ocgt.setEfficiencyTimeSeries(ocgtEfficiencyTimeSeries);
		ocgt.setFixedOperatingCostTimeSeries(ocgtFixedOperatingCostTimeSeries);
		ocgt.setInvestmentCostTimeSeries(ocgtInvestmentCostTimeSeries);
		Set<Substance> ocgtFuels = new HashSet<>();
		ocgtFuels.add(naturalGas);
		ocgt.setFuels(ocgtFuels);

		GeometricTrend oilPGTInvestmentCostTimeSeries = new GeometricTrend();
		oilPGTInvestmentCostTimeSeries.setStart(250000);

		GeometricTrend oilPGTFixedOperatingCostTimeSeries = new GeometricTrend();
		oilPGTFixedOperatingCostTimeSeries.setStart(10000);

		GeometricTrend oilPGTEfficiencyTimeSeries = new GeometricTrend();
		oilPGTEfficiencyTimeSeries.setStart(.35);

		PowerGeneratingTechnology oilPGT = reps.createPowerGeneratingTechnology();
		oilPGT.setName("Fuel oil PGT");
		oilPGT.setCapacity(50);
		oilPGT.setIntermittent(false);
		oilPGT.setApplicableForLongTermContract(true);
		oilPGT.setPeakSegmentDependentAvailability(1);
		oilPGT.setBaseSegmentDependentAvailability(1);
		oilPGT.setMaximumInstalledCapacityFractionPerAgent(1);
		oilPGT.setMaximumInstalledCapacityFractionInCountry(1);
		oilPGT.setMinimumFuelQuality(1);
		oilPGT.setExpectedPermittime(0);
		oilPGT.setExpectedLeadtime(1);
		oilPGT.setExpectedLifetime(30);
		oilPGT.setFixedOperatingCostModifierAfterLifetime(.05);
		oilPGT.setMinimumRunningHours(0);
		oilPGT.setDepreciationTime(15);
		oilPGT.setEfficiencyTimeSeries(oilPGTEfficiencyTimeSeries);
		oilPGT.setFixedOperatingCostTimeSeries(oilPGTFixedOperatingCostTimeSeries);
		oilPGT.setInvestmentCostTimeSeries(oilPGTInvestmentCostTimeSeries);
		Set<Substance> oilPGTFuels = new HashSet<>();
		oilPGTFuels.add(fuelOil);
		oilPGT.setFuels(oilPGTFuels);

		GeometricTrend nuclearPGTInvestmentCostTimeSeries = new GeometricTrend();
		nuclearPGTInvestmentCostTimeSeries.setStart(2874800);

		GeometricTrend nuclearPGTFixedOperatingCostTimeSeries = new GeometricTrend();
		nuclearPGTFixedOperatingCostTimeSeries.setStart(71870);

		GeometricTrend nuclearPGTEfficiencyTimeSeries = new GeometricTrend();
		nuclearPGTEfficiencyTimeSeries.setStart(.33);

		PowerGeneratingTechnology nuclearPGT = reps.createPowerGeneratingTechnology();
		nuclearPGT.setName("Nuclear PGT");
		nuclearPGT.setCapacity(1000);
		nuclearPGT.setIntermittent(false);
		nuclearPGT.setApplicableForLongTermContract(true);
		nuclearPGT.setPeakSegmentDependentAvailability(1);
		nuclearPGT.setBaseSegmentDependentAvailability(1);
		nuclearPGT.setMaximumInstalledCapacityFractionPerAgent(0);
		nuclearPGT.setMaximumInstalledCapacityFractionInCountry(0);
		nuclearPGT.setMinimumFuelQuality(1);
		nuclearPGT.setExpectedPermittime(2);
		nuclearPGT.setExpectedLeadtime(5);
		nuclearPGT.setExpectedLifetime(40);
		nuclearPGT.setFixedOperatingCostModifierAfterLifetime(.05);
		nuclearPGT.setMinimumRunningHours(5000);
		nuclearPGT.setDepreciationTime(25);
		nuclearPGT.setEfficiencyTimeSeries(nuclearPGTEfficiencyTimeSeries);
		nuclearPGT.setFixedOperatingCostTimeSeries(nuclearPGTFixedOperatingCostTimeSeries);
		nuclearPGT.setInvestmentCostTimeSeries(nuclearPGTInvestmentCostTimeSeries);
		Set<Substance> nuclearPGTFuels = new HashSet<>();
		nuclearPGTFuels.add(uranium);
		nuclearPGT.setFuels(nuclearPGTFuels);
		
		TimeSeriesCSVReader pvInvestmentCostTimeSeries = new TimeSeriesCSVReader();
		pvInvestmentCostTimeSeries.setFilename("/data/learningCurves.csv");
		pvInvestmentCostTimeSeries.setDelimiter(",");
		pvInvestmentCostTimeSeries.setStartingYear(-50);
		pvInvestmentCostTimeSeries.setVariableName("PV_Inv");

		TimeSeriesCSVReader pvFixedOperatingCostTimeSeries = new TimeSeriesCSVReader();
		pvFixedOperatingCostTimeSeries.setFilename("/data/learningCurves.csv");
		pvFixedOperatingCostTimeSeries.setDelimiter(",");
		pvFixedOperatingCostTimeSeries.setStartingYear(-50);
		pvFixedOperatingCostTimeSeries.setVariableName("PV_OM");
		
		TimeSeriesCSVReader pvEfficiencyTimeSeries = new TimeSeriesCSVReader();
		pvEfficiencyTimeSeries.setFilename("/data/learningCurves.csv");
		pvEfficiencyTimeSeries.setDelimiter(",");
		pvEfficiencyTimeSeries.setStartingYear(-50);
		pvEfficiencyTimeSeries.setVariableName("PV_Eff");

		PowerGeneratingTechnology pv = reps.createPowerGeneratingTechnology();
		pv.setName("Photovoltaic PGT");
		pv.setCapacity(500);
		pv.setIntermittent(true);
		pv.setApplicableForLongTermContract(true);
		pv.setPeakSegmentDependentAvailability(0.08);
		pv.setBaseSegmentDependentAvailability(0.16);
		pv.setMaximumInstalledCapacityFractionPerAgent(1);
		pv.setMaximumInstalledCapacityFractionInCountry(1);
		pv.setMinimumFuelQuality(1);
		pv.setExpectedPermittime(0);
		pv.setExpectedLeadtime(1);
		pv.setExpectedLifetime(25);
		pv.setFixedOperatingCostModifierAfterLifetime(.05);
		pv.setMinimumRunningHours(0);
		pv.setDepreciationTime(15);
		pv.setEfficiencyTimeSeries(pvEfficiencyTimeSeries);
		pv.setFixedOperatingCostTimeSeries(pvFixedOperatingCostTimeSeries);
		pv.setInvestmentCostTimeSeries(pvInvestmentCostTimeSeries);
		Set<Substance> pvPGTFuels = new HashSet<>();
		pv.setFuels(pvPGTFuels);

		GeometricTrend hydroInvestmentCostTimeSeries = new GeometricTrend();
		hydroInvestmentCostTimeSeries.setStart(800000);

		GeometricTrend hydroFixedOperatingCostTimeSeries = new GeometricTrend();
		hydroFixedOperatingCostTimeSeries.setStart(10000);

		GeometricTrend hydroEfficiencyTimeSeries = new GeometricTrend();
		hydroEfficiencyTimeSeries.setStart(.9);

		PowerGeneratingTechnology hydro = reps.createPowerGeneratingTechnology();
		hydro.setName("Hydroelectric");
		hydro.setCapacity(250);
		hydro.setApplicableForLongTermContract(true);
		hydro.setPeakSegmentDependentAvailability(0.08);
		hydro.setBaseSegmentDependentAvailability(0.16);
		hydro.setMaximumInstalledCapacityFractionPerAgent(0);
		hydro.setMaximumInstalledCapacityFractionInCountry(0);
		hydro.setMinimumFuelQuality(1);
		hydro.setExpectedPermittime(2);
		hydro.setExpectedLeadtime(5);
		hydro.setExpectedLifetime(50);
		hydro.setFixedOperatingCostModifierAfterLifetime(.05);
		hydro.setMinimumRunningHours(0);
		hydro.setDepreciationTime(30);
		hydro.setEfficiencyTimeSeries(hydroEfficiencyTimeSeries);
		hydro.setFixedOperatingCostTimeSeries(hydroFixedOperatingCostTimeSeries);
		hydro.setInvestmentCostTimeSeries(hydroInvestmentCostTimeSeries);
		Set<Substance> hydroPGTFuels = new HashSet<>();
		hydro.setFuels(hydroPGTFuels);
		
		
		TimeSeriesCSVReader windOnshoreInvestmentCostTimeSeries = new TimeSeriesCSVReader();
		windOnshoreInvestmentCostTimeSeries.setFilename("/data/learningCurves.csv");
		windOnshoreInvestmentCostTimeSeries.setDelimiter(",");
		windOnshoreInvestmentCostTimeSeries.setStartingYear(-50);
		windOnshoreInvestmentCostTimeSeries.setVariableName("Wind_Inv");

		TimeSeriesCSVReader windOnshoreFixedOperatingCostTimeSeries = new TimeSeriesCSVReader();
		windOnshoreFixedOperatingCostTimeSeries.setFilename("/data/learningCurves.csv");
		windOnshoreFixedOperatingCostTimeSeries.setDelimiter(",");
		windOnshoreFixedOperatingCostTimeSeries.setStartingYear(-50);
		windOnshoreFixedOperatingCostTimeSeries.setVariableName("Wind_OM");
		
		TimeSeriesCSVReader windOnshoreEfficiencyTimeSeries = new TimeSeriesCSVReader();
		windOnshoreEfficiencyTimeSeries.setFilename("/data/learningCurves.csv");
		windOnshoreEfficiencyTimeSeries.setDelimiter(",");
		windOnshoreEfficiencyTimeSeries.setStartingYear(-50);
		windOnshoreEfficiencyTimeSeries.setVariableName("Wind_Eff");


		PowerGeneratingTechnology windOnshore = reps.createPowerGeneratingTechnology();
		windOnshore.setName("Onshore wind PGT");
		windOnshore.setCapacity(600);
		windOnshore.setIntermittent(true);
		windOnshore.setApplicableForLongTermContract(true);
		windOnshore.setPeakSegmentDependentAvailability(0.05);
		windOnshore.setBaseSegmentDependentAvailability(0.40);
		windOnshore.setMaximumInstalledCapacityFractionPerAgent(1);
		windOnshore.setMaximumInstalledCapacityFractionInCountry(1);
		windOnshore.setMinimumFuelQuality(1);
		windOnshore.setExpectedPermittime(1);
		windOnshore.setExpectedLeadtime(1);
		windOnshore.setExpectedLifetime(25);
		windOnshore.setFixedOperatingCostModifierAfterLifetime(.05);
		windOnshore.setMinimumRunningHours(0);
		windOnshore.setDepreciationTime(15);
		windOnshore.setEfficiencyTimeSeries(windOnshoreEfficiencyTimeSeries);
		windOnshore.setFixedOperatingCostTimeSeries(windOnshoreFixedOperatingCostTimeSeries);
		windOnshore.setInvestmentCostTimeSeries(windOnshoreInvestmentCostTimeSeries);        
		Set<Substance> windOnshorePGTFuels = new HashSet<>();
		windOnshore.setFuels(windOnshorePGTFuels);
		
		TimeSeriesCSVReader windOffshoreInvestmentCostTimeSeries = new TimeSeriesCSVReader();
		windOffshoreInvestmentCostTimeSeries.setFilename("/data/learningCurves.csv");
		windOffshoreInvestmentCostTimeSeries.setDelimiter(",");
		windOffshoreInvestmentCostTimeSeries.setStartingYear(-50);
		windOffshoreInvestmentCostTimeSeries.setVariableName("WindOffshore_Inv");

		TimeSeriesCSVReader windOffshoreFixedOperatingCostTimeSeries = new TimeSeriesCSVReader();
		windOffshoreFixedOperatingCostTimeSeries.setFilename("/data/learningCurves.csv");
		windOffshoreFixedOperatingCostTimeSeries.setDelimiter(",");
		windOffshoreFixedOperatingCostTimeSeries.setStartingYear(-50);
		windOffshoreFixedOperatingCostTimeSeries.setVariableName("WindOffshore_OM");
		
		TimeSeriesCSVReader windOffshoreEfficiencyTimeSeries = new TimeSeriesCSVReader();
		windOffshoreEfficiencyTimeSeries.setFilename("/data/learningCurves.csv");
		windOffshoreEfficiencyTimeSeries.setDelimiter(",");
		windOffshoreEfficiencyTimeSeries.setStartingYear(-50);
		windOffshoreEfficiencyTimeSeries.setVariableName("WindOffshore_Eff");
		

		PowerGeneratingTechnology windOffshore = reps.createPowerGeneratingTechnology();
		windOffshore.setName("Offshore wind PGT");
		windOffshore.setCapacity(600);
		windOffshore.setIntermittent(true);
		windOffshore.setApplicableForLongTermContract(true);
		windOffshore.setPeakSegmentDependentAvailability(0.08);
		windOffshore.setBaseSegmentDependentAvailability(0.65);
		windOffshore.setMaximumInstalledCapacityFractionPerAgent(1);
		windOffshore.setMaximumInstalledCapacityFractionInCountry(1);
		windOffshore.setMinimumFuelQuality(1);
		windOffshore.setExpectedPermittime(1);
		windOffshore.setExpectedLeadtime(2);
		windOffshore.setExpectedLifetime(25);
		windOffshore.setFixedOperatingCostModifierAfterLifetime(.05);
		windOffshore.setMinimumRunningHours(0);
		windOffshore.setDepreciationTime(15);
		windOffshore.setEfficiencyTimeSeries(windOnshoreEfficiencyTimeSeries);
		windOffshore.setFixedOperatingCostTimeSeries(windOnshoreFixedOperatingCostTimeSeries);
		windOffshore.setInvestmentCostTimeSeries(windOnshoreInvestmentCostTimeSeries); 
		Set<Substance> windOffshorePGTFuels = new HashSet<>();
		windOffshore.setFuels(windOffshorePGTFuels);
		
		// Technologies in which incumbents invest
		ArrayList<PowerGeneratingTechnology> conventionalPowerGeneratingTechnologies = new ArrayList<>(); 
		conventionalPowerGeneratingTechnologies.add(nuclearPGT);
		conventionalPowerGeneratingTechnologies.add(lignitePSC);
		conventionalPowerGeneratingTechnologies.add(hydro);
		conventionalPowerGeneratingTechnologies.add(oilPGT);
		conventionalPowerGeneratingTechnologies.add(ocgt);
		conventionalPowerGeneratingTechnologies.add(ccgt);
		conventionalPowerGeneratingTechnologies.add(biomassCHP);
		conventionalPowerGeneratingTechnologies.add(coalPSC);


		// Profiles for intermittent technologies
		windProfileOnShoreNL.setIntermittentTechnology(windOnshore);
		windProfileOffShoreNL.setIntermittentTechnology(windOffshore);
		solarProfileNL.setIntermittentTechnology(pv);

		windProfileOnShoreDE.setIntermittentTechnology(windOnshore);
		windProfileOffShoreDE.setIntermittentTechnology(windOffshore);
		solarProfileDE.setIntermittentTechnology(pv);
		

		// ——————————————————————————————————————————————————
		// Producers with preferences 
		// ——————————————————————————————————————————————————
		
		reps.emlabModel.setEmpiricalPreferenceActive(true);
	
		// Must correspond to names used below in technology definition
		String utilityLevelsTechnologyPV = "Photovoltaic PGT";
		String utilityLevelsTechnologyOnshore ="Onshore wind PGT";
		String utilityLevelsTechnologyOffshore = "Offshore wind PGT";
		String utilityLevelsReturn5 = "5%";
		String utilityLevelsReturn6 = "6%";
		String utilityLevelsReturn7 = "7%";
		String utilityLevelsCountryOwn = "Own country";
		String utilityLevelsCountryKnown = "Known country";
		String utilityLevelsCountryUnknown = "Unknown country";
		String utilityLevelsPolicyFIT = "FIT";
		String utilityLevelsPolicyAuction = "Auction";
		String utilityLevelsPolicyNone = "None";

		HashMap<String, Double> preferenceMap;
		HashSet<ElectricitySpotMarket> potentialInvestorMarkets;

		InvestInPowerGenerationTechnologiesWithTenderAndPreferencesRole tenderAndPreferenceInvestmentRole = new InvestInPowerGenerationTechnologiesWithTenderAndPreferencesRole(schedule);

		tenderAndPreferenceInvestmentRole.setRandomUtilityBound(0.05); // adding some randomness according to random utility theory

		EnergyProducer prefInvestorsSmallDE = reps.createEnergyProducer();
		prefInvestorsSmallDE.setName("Pref Investor Small DE");
		prefInvestorsSmallDE.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsSmallDE.setPriceMarkUp(1.0);
		prefInvestorsSmallDE.setWillingToInvest(true);
		prefInvestorsSmallDE.setDownpaymentFractionOfCash(.5);
		prefInvestorsSmallDE.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsSmallDE.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsSmallDE.setDebtRatioOfInvestments(0.7);
		prefInvestorsSmallDE.setLoanInterestRate(0.1);
		prefInvestorsSmallDE.setEquityInterestRate(0.1);
		prefInvestorsSmallDE.setPastTimeHorizon(5);
		prefInvestorsSmallDE.setInvestmentFutureTimeHorizon(7);
		prefInvestorsSmallDE.setLongTermContractPastTimeHorizon(3);
		prefInvestorsSmallDE.setLongTermContractMargin(0.1);
		prefInvestorsSmallDE.setCash(3e9);
		prefInvestorsSmallDE.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 100.6);
		preferenceMap.put(utilityLevelsCountryKnown, -99.4);
		preferenceMap.put(utilityLevelsCountryUnknown, -1.2);
		prefInvestorsSmallDE.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -29.6);
		preferenceMap.put(utilityLevelsReturn6, -6.8);
		preferenceMap.put(utilityLevelsReturn7, 36.4);
		prefInvestorsSmallDE.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 28.4);
		preferenceMap.put(utilityLevelsPolicyAuction, -2.3);
		preferenceMap.put(utilityLevelsPolicyNone, -26.1);
		prefInvestorsSmallDE.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 25.2);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 9.3);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -34.4);
		prefInvestorsSmallDE.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(germanyElectricitySpotMarket);
		prefInvestorsSmallDE.setInvestorMarket(germanyElectricitySpotMarket);
		prefInvestorsSmallDE.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsMediumDE = reps.createEnergyProducer();
		prefInvestorsMediumDE.setName("Pref Investor Medium DE");
		prefInvestorsMediumDE.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsMediumDE.setPriceMarkUp(1.0);
		prefInvestorsMediumDE.setWillingToInvest(true);
		prefInvestorsMediumDE.setDownpaymentFractionOfCash(.5);
		prefInvestorsMediumDE.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsMediumDE.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsMediumDE.setDebtRatioOfInvestments(0.7);
		prefInvestorsMediumDE.setLoanInterestRate(0.1);
		prefInvestorsMediumDE.setEquityInterestRate(0.1);
		prefInvestorsMediumDE.setPastTimeHorizon(5);
		prefInvestorsMediumDE.setInvestmentFutureTimeHorizon(7);
		prefInvestorsMediumDE.setLongTermContractPastTimeHorizon(3);
		prefInvestorsMediumDE.setLongTermContractMargin(0.1);
		prefInvestorsMediumDE.setCash(3e9);
		prefInvestorsMediumDE.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 78.0);
		preferenceMap.put(utilityLevelsCountryKnown, -57.8);
		preferenceMap.put(utilityLevelsCountryUnknown, -20.2);
		prefInvestorsMediumDE.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -39.8);
		preferenceMap.put(utilityLevelsReturn6, 2.7);
		preferenceMap.put(utilityLevelsReturn7, 37.1);
		prefInvestorsMediumDE.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 28.8);
		preferenceMap.put(utilityLevelsPolicyAuction, -2.5);
		preferenceMap.put(utilityLevelsPolicyNone, -26.3);
		prefInvestorsMediumDE.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 31.6);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 31.5);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -63.2);
		prefInvestorsMediumDE.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(germanyElectricitySpotMarket);
		prefInvestorsMediumDE.setInvestorMarket(germanyElectricitySpotMarket);
		prefInvestorsMediumDE.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsLargeDE = reps.createEnergyProducer();
		prefInvestorsLargeDE.setName("Pref Investor Large DE");
		prefInvestorsLargeDE.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsLargeDE.setPriceMarkUp(1.0);
		prefInvestorsLargeDE.setWillingToInvest(true);
		prefInvestorsLargeDE.setDownpaymentFractionOfCash(.5);
		prefInvestorsLargeDE.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsLargeDE.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsLargeDE.setDebtRatioOfInvestments(0.7);
		prefInvestorsLargeDE.setLoanInterestRate(0.1);
		prefInvestorsLargeDE.setEquityInterestRate(0.1);
		prefInvestorsLargeDE.setPastTimeHorizon(5);
		prefInvestorsLargeDE.setInvestmentFutureTimeHorizon(7);
		prefInvestorsLargeDE.setLongTermContractPastTimeHorizon(3);
		prefInvestorsLargeDE.setLongTermContractMargin(0.1);
		prefInvestorsLargeDE.setCash(3e9);
		prefInvestorsLargeDE.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 42.4);
		preferenceMap.put(utilityLevelsCountryKnown, -9.8);
		preferenceMap.put(utilityLevelsCountryUnknown, -32.6);
		prefInvestorsLargeDE.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -48.0);
		preferenceMap.put(utilityLevelsReturn6, 0.1);
		preferenceMap.put(utilityLevelsReturn7, 47.9);
		prefInvestorsLargeDE.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 39.2);
		preferenceMap.put(utilityLevelsPolicyAuction, 1.4);
		preferenceMap.put(utilityLevelsPolicyNone, -40.5);
		prefInvestorsLargeDE.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 43.1);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 25.7);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -68.9);
		prefInvestorsLargeDE.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(germanyElectricitySpotMarket);
		prefInvestorsLargeDE.setInvestorMarket(germanyElectricitySpotMarket);
		prefInvestorsLargeDE.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsVerylargeDE = reps.createEnergyProducer();
		prefInvestorsVerylargeDE.setName("Pref Investor Verylarge DE");
		prefInvestorsVerylargeDE.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsVerylargeDE.setPriceMarkUp(1.0);
		prefInvestorsVerylargeDE.setWillingToInvest(true);
		prefInvestorsVerylargeDE.setDownpaymentFractionOfCash(.5);
		prefInvestorsVerylargeDE.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsVerylargeDE.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsVerylargeDE.setDebtRatioOfInvestments(0.7);
		prefInvestorsVerylargeDE.setLoanInterestRate(0.1);
		prefInvestorsVerylargeDE.setEquityInterestRate(0.1);
		prefInvestorsVerylargeDE.setPastTimeHorizon(5);
		prefInvestorsVerylargeDE.setInvestmentFutureTimeHorizon(7);
		prefInvestorsVerylargeDE.setLongTermContractPastTimeHorizon(3);
		prefInvestorsVerylargeDE.setLongTermContractMargin(0.1);
		prefInvestorsVerylargeDE.setCash(3e9);
		prefInvestorsVerylargeDE.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 44.9);
		preferenceMap.put(utilityLevelsCountryKnown, -0.7);
		preferenceMap.put(utilityLevelsCountryUnknown, -44.2);
		prefInvestorsVerylargeDE.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -27.3);
		preferenceMap.put(utilityLevelsReturn6, -9.1);
		preferenceMap.put(utilityLevelsReturn7, 36.5);
		prefInvestorsVerylargeDE.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 40.6);
		preferenceMap.put(utilityLevelsPolicyAuction, 5.4);
		preferenceMap.put(utilityLevelsPolicyNone, -46.0);
		prefInvestorsVerylargeDE.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 16.1);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 6.9);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -23.0);
		prefInvestorsVerylargeDE.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(germanyElectricitySpotMarket);
		prefInvestorsVerylargeDE.setInvestorMarket(germanyElectricitySpotMarket);
		prefInvestorsVerylargeDE.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsSmallNL = reps.createEnergyProducer();
		prefInvestorsSmallNL.setName("Pref Investor Small NL");
		prefInvestorsSmallNL.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsSmallNL.setPriceMarkUp(1.0);
		prefInvestorsSmallNL.setWillingToInvest(true);
		prefInvestorsSmallNL.setDownpaymentFractionOfCash(.5);
		prefInvestorsSmallNL.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsSmallNL.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsSmallNL.setDebtRatioOfInvestments(0.7);
		prefInvestorsSmallNL.setLoanInterestRate(0.1);
		prefInvestorsSmallNL.setEquityInterestRate(0.1);
		prefInvestorsSmallNL.setPastTimeHorizon(5);
		prefInvestorsSmallNL.setInvestmentFutureTimeHorizon(7);
		prefInvestorsSmallNL.setLongTermContractPastTimeHorizon(3);
		prefInvestorsSmallNL.setLongTermContractMargin(0.1);
		prefInvestorsSmallNL.setCash(3e9);
		prefInvestorsSmallNL.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 100.6);
		preferenceMap.put(utilityLevelsCountryKnown, -99.4);
		preferenceMap.put(utilityLevelsCountryUnknown, -1.2);
		prefInvestorsSmallNL.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -29.6);
		preferenceMap.put(utilityLevelsReturn6, -6.8);
		preferenceMap.put(utilityLevelsReturn7, 36.4);
		prefInvestorsSmallNL.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 28.4);
		preferenceMap.put(utilityLevelsPolicyAuction, -2.3);
		preferenceMap.put(utilityLevelsPolicyNone, -26.1);
		prefInvestorsSmallNL.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 25.2);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 9.3);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -34.4);
		prefInvestorsSmallNL.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(netherlandsElectricitySpotMarket);
		prefInvestorsSmallNL.setInvestorMarket(netherlandsElectricitySpotMarket);
		prefInvestorsSmallNL.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsMediumNL = reps.createEnergyProducer();
		prefInvestorsMediumNL.setName("Pref Investor Medium NL");
		prefInvestorsMediumNL.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsMediumNL.setPriceMarkUp(1.0);
		prefInvestorsMediumNL.setWillingToInvest(true);
		prefInvestorsMediumNL.setDownpaymentFractionOfCash(.5);
		prefInvestorsMediumNL.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsMediumNL.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsMediumNL.setDebtRatioOfInvestments(0.7);
		prefInvestorsMediumNL.setLoanInterestRate(0.1);
		prefInvestorsMediumNL.setEquityInterestRate(0.1);
		prefInvestorsMediumNL.setPastTimeHorizon(5);
		prefInvestorsMediumNL.setInvestmentFutureTimeHorizon(7);
		prefInvestorsMediumNL.setLongTermContractPastTimeHorizon(3);
		prefInvestorsMediumNL.setLongTermContractMargin(0.1);
		prefInvestorsMediumNL.setCash(3e9);
		prefInvestorsMediumNL.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 78.0);
		preferenceMap.put(utilityLevelsCountryKnown, -57.8);
		preferenceMap.put(utilityLevelsCountryUnknown, -20.2);
		prefInvestorsMediumNL.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -39.8);
		preferenceMap.put(utilityLevelsReturn6, 2.7);
		preferenceMap.put(utilityLevelsReturn7, 37.1);
		prefInvestorsMediumNL.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 28.8);
		preferenceMap.put(utilityLevelsPolicyAuction, -2.5);
		preferenceMap.put(utilityLevelsPolicyNone, -26.3);
		prefInvestorsMediumNL.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 31.6);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 31.5);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -63.2);
		prefInvestorsMediumNL.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(netherlandsElectricitySpotMarket);
		prefInvestorsMediumNL.setInvestorMarket(netherlandsElectricitySpotMarket);
		prefInvestorsMediumNL.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsLargeNL = reps.createEnergyProducer();
		prefInvestorsLargeNL.setName("Pref Investor Large NL");
		prefInvestorsLargeNL.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsLargeNL.setPriceMarkUp(1.0);
		prefInvestorsLargeNL.setWillingToInvest(true);
		prefInvestorsLargeNL.setDownpaymentFractionOfCash(.5);
		prefInvestorsLargeNL.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsLargeNL.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsLargeNL.setDebtRatioOfInvestments(0.7);
		prefInvestorsLargeNL.setLoanInterestRate(0.1);
		prefInvestorsLargeNL.setEquityInterestRate(0.1);
		prefInvestorsLargeNL.setPastTimeHorizon(5);
		prefInvestorsLargeNL.setInvestmentFutureTimeHorizon(7);
		prefInvestorsLargeNL.setLongTermContractPastTimeHorizon(3);
		prefInvestorsLargeNL.setLongTermContractMargin(0.1);
		prefInvestorsLargeNL.setCash(3e9);
		prefInvestorsLargeNL.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 42.4);
		preferenceMap.put(utilityLevelsCountryKnown, -9.8);
		preferenceMap.put(utilityLevelsCountryUnknown, -32.6);
		prefInvestorsLargeNL.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -48.0);
		preferenceMap.put(utilityLevelsReturn6, 0.1);
		preferenceMap.put(utilityLevelsReturn7, 47.9);
		prefInvestorsLargeNL.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 39.2);
		preferenceMap.put(utilityLevelsPolicyAuction, 1.4);
		preferenceMap.put(utilityLevelsPolicyNone, -40.5);
		prefInvestorsLargeNL.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 43.1);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 25.7);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -68.9);
		prefInvestorsLargeNL.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(netherlandsElectricitySpotMarket);
		prefInvestorsLargeNL.setInvestorMarket(netherlandsElectricitySpotMarket);
		prefInvestorsLargeNL.setPotentialInvestorMarkets(potentialInvestorMarkets);

		EnergyProducer prefInvestorsVerylargeNL = reps.createEnergyProducer();
		prefInvestorsVerylargeNL.setName("Pref Investor Verylarge NL");
		prefInvestorsVerylargeNL.setNumberOfYearsBacklookingForForecasting(5);
		prefInvestorsVerylargeNL.setPriceMarkUp(1.0);
		prefInvestorsVerylargeNL.setWillingToInvest(true);
		prefInvestorsVerylargeNL.setDownpaymentFractionOfCash(.5);
		prefInvestorsVerylargeNL.setDismantlingRequiredOperatingProfit(0);
		prefInvestorsVerylargeNL.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		prefInvestorsVerylargeNL.setDebtRatioOfInvestments(0.7);
		prefInvestorsVerylargeNL.setLoanInterestRate(0.1);
		prefInvestorsVerylargeNL.setEquityInterestRate(0.1);
		prefInvestorsVerylargeNL.setPastTimeHorizon(5);
		prefInvestorsVerylargeNL.setInvestmentFutureTimeHorizon(7);
		prefInvestorsVerylargeNL.setLongTermContractPastTimeHorizon(3);
		prefInvestorsVerylargeNL.setLongTermContractMargin(0.1);
		prefInvestorsVerylargeNL.setCash(3e9);
		prefInvestorsVerylargeNL.setInvestmentRole(tenderAndPreferenceInvestmentRole); 

		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsCountryOwn, 44.9);
		preferenceMap.put(utilityLevelsCountryKnown, -0.7);
		preferenceMap.put(utilityLevelsCountryUnknown, -44.2);
		prefInvestorsVerylargeNL.setUtilityCountry(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsReturn5, -27.3);
		preferenceMap.put(utilityLevelsReturn6, -9.1);
		preferenceMap.put(utilityLevelsReturn7, 36.5);
		prefInvestorsVerylargeNL.setUtilityReturn(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsPolicyFIT, 40.6);
		preferenceMap.put(utilityLevelsPolicyAuction, 5.4);
		preferenceMap.put(utilityLevelsPolicyNone, -46.0);
		prefInvestorsVerylargeNL.setUtilityPolicy(preferenceMap);
		preferenceMap = new HashMap<>();
		preferenceMap.put(utilityLevelsTechnologyPV, 16.1);
		preferenceMap.put(utilityLevelsTechnologyOnshore, 6.9);
		preferenceMap.put(utilityLevelsTechnologyOffshore, -23.0);
		prefInvestorsVerylargeNL.setUtilityTechnology(preferenceMap);

		potentialInvestorMarkets = new HashSet<>();
		//potentialInvestorMarkets.add();
		potentialInvestorMarkets.add(netherlandsElectricitySpotMarket);
		prefInvestorsVerylargeNL.setInvestorMarket(netherlandsElectricitySpotMarket);
		prefInvestorsVerylargeNL.setPotentialInvestorMarkets(potentialInvestorMarkets);


		// ——————————————————————————————————————————————————
		// Producers (other)
		// ——————————————————————————————————————————————————

		InvestInPowerGenerationTechnologiesWithTenderRole tenderInvestmentRole = new InvestInPowerGenerationTechnologiesWithTenderRole(schedule);

		EnergyProducer energyProducerNLA = reps.createEnergyProducer();
		energyProducerNLA.setName("Energy Producer NL A");
		energyProducerNLA.setInvestorMarket(netherlandsElectricitySpotMarket);
		energyProducerNLA.setNumberOfYearsBacklookingForForecasting(5);
		energyProducerNLA.setPriceMarkUp(1.0);
		energyProducerNLA.setWillingToInvest(true);
		energyProducerNLA.setDownpaymentFractionOfCash(.5);
		energyProducerNLA.setDismantlingRequiredOperatingProfit(0);
		energyProducerNLA.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		energyProducerNLA.setDebtRatioOfInvestments(0.7);
		energyProducerNLA.setLoanInterestRate(0.1);
		energyProducerNLA.setEquityInterestRate(0.1);
		energyProducerNLA.setPastTimeHorizon(5);
		energyProducerNLA.setInvestmentFutureTimeHorizon(7);
		energyProducerNLA.setLongTermContractPastTimeHorizon(3);
		energyProducerNLA.setLongTermContractMargin(0.1);
		energyProducerNLA.setCash(3e9);
		energyProducerNLA.setInvestmentRole(tenderInvestmentRole);
		energyProducerNLA.setPotentialPowerGeneratingTechnologies(conventionalPowerGeneratingTechnologies);

		EnergyProducer energyProducerNLB = reps.createEnergyProducer();
		energyProducerNLB.setName("Energy Producer NL B");
		energyProducerNLB.setInvestorMarket(netherlandsElectricitySpotMarket);
		energyProducerNLB.setNumberOfYearsBacklookingForForecasting(5);
		energyProducerNLB.setPriceMarkUp(1.0);
		energyProducerNLB.setWillingToInvest(true);
		energyProducerNLB.setDownpaymentFractionOfCash(.5);
		energyProducerNLB.setDismantlingRequiredOperatingProfit(0);
		energyProducerNLB.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		energyProducerNLB.setDebtRatioOfInvestments(0.7);
		energyProducerNLB.setLoanInterestRate(0.1);
		energyProducerNLB.setEquityInterestRate(0.1);
		energyProducerNLB.setPastTimeHorizon(5);
		energyProducerNLB.setInvestmentFutureTimeHorizon(7);
		energyProducerNLB.setLongTermContractPastTimeHorizon(3);
		energyProducerNLB.setLongTermContractMargin(0.1);
		energyProducerNLB.setCash(3e9);
		energyProducerNLB.setInvestmentRole(tenderInvestmentRole);
		energyProducerNLB.setPotentialPowerGeneratingTechnologies(conventionalPowerGeneratingTechnologies);

		EnergyProducer energyProducerNLC = reps.createEnergyProducer();
		energyProducerNLC.setName("Energy Producer NL C");
		energyProducerNLC.setInvestorMarket(netherlandsElectricitySpotMarket);
		energyProducerNLC.setNumberOfYearsBacklookingForForecasting(5);
		energyProducerNLC.setPriceMarkUp(1.0);
		energyProducerNLC.setWillingToInvest(true);
		energyProducerNLC.setDownpaymentFractionOfCash(.5);
		energyProducerNLC.setDismantlingRequiredOperatingProfit(0);
		energyProducerNLC.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		energyProducerNLC.setDebtRatioOfInvestments(0.7);
		energyProducerNLC.setLoanInterestRate(0.1);
		energyProducerNLC.setEquityInterestRate(0.1);
		energyProducerNLC.setPastTimeHorizon(5);
		energyProducerNLC.setInvestmentFutureTimeHorizon(7);
		energyProducerNLC.setLongTermContractPastTimeHorizon(3);
		energyProducerNLC.setLongTermContractMargin(0.1);
		energyProducerNLC.setCash(3e9);
		energyProducerNLC.setInvestmentRole(tenderInvestmentRole);
		energyProducerNLC.setPotentialPowerGeneratingTechnologies(conventionalPowerGeneratingTechnologies);

		EnergyProducer energyProducerDEA = reps.createEnergyProducer();
		energyProducerDEA.setName("Energy Producer DE A");
		energyProducerDEA.setInvestorMarket(germanyElectricitySpotMarket);
		energyProducerDEA.setNumberOfYearsBacklookingForForecasting(5);
		energyProducerDEA.setPriceMarkUp(1.0);
		energyProducerDEA.setWillingToInvest(true);
		energyProducerDEA.setDownpaymentFractionOfCash(.5);
		energyProducerDEA.setDismantlingRequiredOperatingProfit(0);
		energyProducerDEA.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		energyProducerDEA.setDebtRatioOfInvestments(0.7);
		energyProducerDEA.setLoanInterestRate(0.1);
		energyProducerDEA.setEquityInterestRate(0.1);
		energyProducerDEA.setPastTimeHorizon(5);
		energyProducerDEA.setInvestmentFutureTimeHorizon(7);
		energyProducerDEA.setLongTermContractPastTimeHorizon(3);
		energyProducerDEA.setLongTermContractMargin(0.1);
		energyProducerDEA.setCash(3e9);
		energyProducerDEA.setInvestmentRole(tenderInvestmentRole);
		energyProducerDEA.setPotentialPowerGeneratingTechnologies(conventionalPowerGeneratingTechnologies);

		EnergyProducer energyProducerDEB = reps.createEnergyProducer();
		energyProducerDEB.setName("Energy Producer DE B");
		energyProducerDEB.setInvestorMarket(germanyElectricitySpotMarket);
		energyProducerDEB.setNumberOfYearsBacklookingForForecasting(5);
		energyProducerDEB.setPriceMarkUp(1.0);
		energyProducerDEB.setWillingToInvest(true);
		energyProducerDEB.setDownpaymentFractionOfCash(.5);
		energyProducerDEB.setDismantlingRequiredOperatingProfit(0);
		energyProducerDEB.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		energyProducerDEB.setDebtRatioOfInvestments(0.7);
		energyProducerDEB.setLoanInterestRate(0.1);
		energyProducerDEB.setEquityInterestRate(0.1);
		energyProducerDEB.setPastTimeHorizon(5);
		energyProducerDEB.setInvestmentFutureTimeHorizon(7);
		energyProducerDEB.setLongTermContractPastTimeHorizon(3);
		energyProducerDEB.setLongTermContractMargin(0.1);
		energyProducerDEB.setCash(3e9);
		energyProducerDEB.setInvestmentRole(tenderInvestmentRole);
		energyProducerDEB.setPotentialPowerGeneratingTechnologies(conventionalPowerGeneratingTechnologies);

		EnergyProducer energyProducerDEC = reps.createEnergyProducer();
		energyProducerDEC.setName("Energy Producer DE C");
		energyProducerDEC.setInvestorMarket(germanyElectricitySpotMarket);
		energyProducerDEC.setNumberOfYearsBacklookingForForecasting(5);
		energyProducerDEC.setPriceMarkUp(1.0);
		energyProducerDEC.setWillingToInvest(true);
		energyProducerDEC.setDownpaymentFractionOfCash(.5);
		energyProducerDEC.setDismantlingRequiredOperatingProfit(0);
		energyProducerDEC.setDismantlingProlongingYearsAfterTechnicalLifetime(0);
		energyProducerDEC.setDebtRatioOfInvestments(0.7);
		energyProducerDEC.setLoanInterestRate(0.1);
		energyProducerDEC.setEquityInterestRate(0.1);
		energyProducerDEC.setPastTimeHorizon(5);
		energyProducerDEC.setInvestmentFutureTimeHorizon(7);
		energyProducerDEC.setLongTermContractPastTimeHorizon(3);
		energyProducerDEC.setLongTermContractMargin(0.1);
		energyProducerDEC.setCash(3e9);
		energyProducerDEC.setInvestmentRole(tenderInvestmentRole); 
		energyProducerDEC.setPotentialPowerGeneratingTechnologies(conventionalPowerGeneratingTechnologies);
		
		// ——————————————————————————————————————————————————
		// Power Plant Factory
		// ——————————————————————————————————————————————————

		PowerPlantCSVFactory powerPlantCSVFactory = new PowerPlantCSVFactory(reps);
		powerPlantCSVFactory.setCsvFile("/data/separatedDutchGermanPlants2015For14investors.csv");
		for (PowerPlant plant : powerPlantCSVFactory.read()) {
			reps.createPowerPlantFromPlant(plant);
		}
		

		// ——————————————————————————————————————————————————
		// Empirical mapping parameters
		// ——————————————————————————————————————————————————
  
		
		// Based on: 1602242918580-Scenario_NL_DE_intermittent_auction_pref-EMlabModelRole-DefaultReporter

	    EmpiricalMappingFunctionParameter empiricalMappingFunction1 = new EmpiricalMappingFunctionParameter();
			empiricalMappingFunction1.setModelledRoeMin(0.0312290553665691);
	    empiricalMappingFunction1.setModelledRoeMax(0.246137607641006);
	    empiricalMappingFunction1.setIntercept(0.0350961652117127);
	    empiricalMappingFunction1.setSlope(0.186125677999637);
	    empiricalMappingFunction1.setMarket(germanyElectricitySpotMarket);
	    empiricalMappingFunction1.setTechnology(windOnshore); 
	    reps.empiricalMappingFunctionParameters.add(empiricalMappingFunction1);
	    EmpiricalMappingFunctionParameter empiricalMappingFunction2 = new EmpiricalMappingFunctionParameter();
			empiricalMappingFunction2.setModelledRoeMin(0.049401491542954);
	    empiricalMappingFunction2.setModelledRoeMax(0.576690417776646);
	    empiricalMappingFunction2.setIntercept(0.0548899827404762);
	    empiricalMappingFunction2.setSlope(0.0758597383899395);
	    empiricalMappingFunction2.setMarket(germanyElectricitySpotMarket);
	    empiricalMappingFunction2.setTechnology(windOffshore); 
	    reps.empiricalMappingFunctionParameters.add(empiricalMappingFunction2);
	    EmpiricalMappingFunctionParameter empiricalMappingFunction3 = new EmpiricalMappingFunctionParameter();
			empiricalMappingFunction3.setModelledRoeMin(0.122493330952725);
	    empiricalMappingFunction3.setModelledRoeMax(0.221477708947399);
	    empiricalMappingFunction3.setIntercept(-0.0160192329010469);
	    empiricalMappingFunction3.setSlope(0.404104170883937);
	    empiricalMappingFunction3.setMarket(germanyElectricitySpotMarket);
	    empiricalMappingFunction3.setTechnology(pv); 
	    reps.empiricalMappingFunctionParameters.add(empiricalMappingFunction3);
	    EmpiricalMappingFunctionParameter empiricalMappingFunction4 = new EmpiricalMappingFunctionParameter();
			empiricalMappingFunction4.setModelledRoeMin(0.00554999316756439);
	    empiricalMappingFunction4.setModelledRoeMax(0.0201800345922119);
	    empiricalMappingFunction4.setIntercept(0.0405896165317145);
	    empiricalMappingFunction4.setSlope(2.73410025569792);
	    empiricalMappingFunction4.setMarket(netherlandsElectricitySpotMarket);
	    empiricalMappingFunction4.setTechnology(windOnshore); 
	    reps.empiricalMappingFunctionParameters.add(empiricalMappingFunction4);
	    EmpiricalMappingFunctionParameter empiricalMappingFunction5 = new EmpiricalMappingFunctionParameter();
			empiricalMappingFunction5.setModelledRoeMin(0.00336285886775488);
	    empiricalMappingFunction5.setModelledRoeMax(0.0500063884367621);
	    empiricalMappingFunction5.setIntercept(0.0930530732173046);
	    empiricalMappingFunction5.setSlope(0.857568035043781);
	    empiricalMappingFunction5.setMarket(netherlandsElectricitySpotMarket);
	    empiricalMappingFunction5.setTechnology(windOffshore); 
	    reps.empiricalMappingFunctionParameters.add(empiricalMappingFunction5);
	    
		// Based on: 1598437148250-Scenario_NL_DE_intermittent_auction_pref-EMlabModelRole-DefaultReporter


		EmpiricalMappingFunctionParameter empiricalMappingFunction6 = new EmpiricalMappingFunctionParameter();
		empiricalMappingFunction6.setModelledRoeMin(0.00049285033968858);
		empiricalMappingFunction6.setModelledRoeMax(0.0183891030478593);
		empiricalMappingFunction6.setIntercept(0.0551670304070817);
		empiricalMappingFunction6.setSlope(2.23510478155785);
		empiricalMappingFunction6.setMarket(netherlandsElectricitySpotMarket);
		empiricalMappingFunction6.setTechnology(pv); 
		reps.empiricalMappingFunctionParameters.add(empiricalMappingFunction6);

		// ——————————————————————————————————————————————————
		// Policy Module
		// ——————————————————————————————————————————————————


		// Phase-out

		HashMap<PowerGeneratingTechnology, Integer> schemePhaseOutTimeNL = new HashMap<>();
		schemePhaseOutTimeNL.put(windOnshore, 100);
		schemePhaseOutTimeNL.put(pv, 100);
		schemePhaseOutTimeNL.put(windOffshore, 100);

		HashMap<PowerGeneratingTechnology, Integer> schemePhaseOutTimeDE = new HashMap<>();
		schemePhaseOutTimeDE.put(windOnshore, 100);
		schemePhaseOutTimeDE.put(pv, 100);
		schemePhaseOutTimeDE.put(windOffshore, 100);

		// setups

		reps.emlabModel.setFeedInPremiumImplemented(false);
		reps.emlabModel.setRenewableTenderSchemeImplemented(true);

		Regulator regulatorNl = new Regulator();
		regulatorNl.setName("regulatorNetherlands");
		regulatorNl.setNumberOfYearsLookingBackToForecastDemand(4);
		regulatorNl.setZone(nl);
		reps.regulators.add(regulatorNl);


		// Feed-in Premium details
		RenewableSupportFipScheme premiumNL = new RenewableSupportFipScheme();
		premiumNL.setName("Renewable Support Scheme");
		premiumNL.setSupportSchemeDuration(17);
		premiumNL.setEmRevenuePaidExpost(true);
		premiumNL.setFutureSchemeStartTime(5);
		premiumNL.setAvgElectricityPriceBasedPremiumEnabled(false);
		premiumNL.setTechnologySpecificityEnabled(true);
		premiumNL.setCostContainmentMechanismEnabled(true);
		premiumNL.setRegulator(regulatorNl);
		premiumNL.setZone(nl);


		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		HashSet<PowerGeneratingTechnology> powerGeneratingTechnologiesEligible = new HashSet<>();
		powerGeneratingTechnologiesEligible.add(windOnshore);
		powerGeneratingTechnologiesEligible.add(pv);
		powerGeneratingTechnologiesEligible.add(windOffshore);
		premiumNL.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligible);


		premiumNL.setFutureSchemePhaseoutTime(schemePhaseOutTimeNL);


		reps.renewableSupportFipSchemes.add(premiumNL);



		// Policy NL
		// ---------------------------------------------



		// Bias factors

		BiasFactor biasFactorNL1Wind  = new BiasFactor();
		biasFactorNL1Wind.setFeedInPremiumBiasFactor(1);
		biasFactorNL1Wind.setDegressionFactor(0.06);
		biasFactorNL1Wind.setNode(nlNode);
		biasFactorNL1Wind.setScheme(premiumNL);
		biasFactorNL1Wind.setTechnology(windOnshore);
		reps.biasFactors.add(biasFactorNL1Wind);

		BiasFactor biasFactorNL1PV  = new BiasFactor();
		biasFactorNL1PV.setFeedInPremiumBiasFactor(1);
		biasFactorNL1PV.setDegressionFactor(0.06);
		biasFactorNL1PV.setNode(nlNode);
		biasFactorNL1PV.setScheme(premiumNL);
		biasFactorNL1PV.setTechnology(pv);
		reps.biasFactors.add(biasFactorNL1PV);

		BiasFactor biasFactorNL1WindOffshore  = new BiasFactor();
		biasFactorNL1WindOffshore.setFeedInPremiumBiasFactor(1);
		biasFactorNL1WindOffshore.setDegressionFactor(0.06);
		biasFactorNL1WindOffshore.setNode(nlNode);
		biasFactorNL1WindOffshore.setScheme(premiumNL);
		biasFactorNL1WindOffshore.setTechnology(windOffshore);
		reps.biasFactors.add(biasFactorNL1WindOffshore);


		// Renewable Tender Scheme
		// ——————————————————————————————————————————————————
				
		long nlPhaseOutTick = 10;
		long nlTenderDuration = 17;

		RenewableSupportSchemeTender renewableSupportSchemeNL1 = new RenewableSupportSchemeTender();
		renewableSupportSchemeNL1.setName("OnshoreTender");
		renewableSupportSchemeNL1.setFutureTenderOperationStartTime(2);
		renewableSupportSchemeNL1.setSupportSchemeDuration(nlTenderDuration);
		renewableSupportSchemeNL1.setSupportSchemePhaseOutTick(50);
		renewableSupportSchemeNL1.setTechnologySpecificityEnabled(true);
		renewableSupportSchemeNL1.setExpostRevenueCalculation(true);
		renewableSupportSchemeNL1.setRegulator(regulatorNl);
		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleNLTender = new HashSet<>();
		powerGeneratingTechnologiesEligibleNLTender.add(windOnshore);
		renewableSupportSchemeNL1.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleNLTender);
		reps.renewableSupportSchemeTenders.add(renewableSupportSchemeNL1);		
		
		
		RenewableSupportSchemeTender renewableSupportSchemeNL2 = new RenewableSupportSchemeTender();
		renewableSupportSchemeNL2.setName("OffshoreTender");
		renewableSupportSchemeNL2.setFutureTenderOperationStartTime(2);
		renewableSupportSchemeNL2.setSupportSchemeDuration(nlTenderDuration);
		renewableSupportSchemeNL2.setSupportSchemePhaseOutTick(50);
		renewableSupportSchemeNL2.setTechnologySpecificityEnabled(true);
		renewableSupportSchemeNL2.setExpostRevenueCalculation(true);
		renewableSupportSchemeNL2.setRegulator(regulatorNl);
		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleNLTender2 = new HashSet<>();
		powerGeneratingTechnologiesEligibleNLTender2.add(windOffshore);
		renewableSupportSchemeNL2.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleNLTender2);
		reps.renewableSupportSchemeTenders.add(renewableSupportSchemeNL2);
		
		RenewableSupportSchemeTender renewableSupportSchemeNL3 = new RenewableSupportSchemeTender();
		renewableSupportSchemeNL3.setName("PVTender");
		renewableSupportSchemeNL3.setFutureTenderOperationStartTime(2);
		renewableSupportSchemeNL3.setSupportSchemeDuration(nlTenderDuration);
		renewableSupportSchemeNL3.setSupportSchemePhaseOutTick(10);
		renewableSupportSchemeNL3.setTechnologySpecificityEnabled(true);
		renewableSupportSchemeNL3.setExpostRevenueCalculation(true);
		renewableSupportSchemeNL3.setRegulator(regulatorNl);
		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleNLTender3 = new HashSet<>();
		powerGeneratingTechnologiesEligibleNLTender3.add(pv);
		renewableSupportSchemeNL3.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleNLTender3);
		reps.renewableSupportSchemeTenders.add(renewableSupportSchemeNL3);
		
		


		// Renewable Targets - Technology neutral
		// ——————————————————————————————————————————————————

		TimeSeriesCSVReader nl_nreap = new TimeSeriesCSVReader();
		nl_nreap.setFilename("/data/NECPPotentials2015.csv");
		nl_nreap.setDelimiter(",");
		nl_nreap.setStartingYear(0);
		nl_nreap.setVariableName("NL_target_Total");

		RenewableTarget renewableTenderTarget = new RenewableTarget();
		renewableTenderTarget.setYearlyRenewableTargetTimeSeries(nl_nreap);
		renewableTenderTarget.setTargetTechnologySpecific(false);
		renewableTenderTarget.setRegulator(regulatorNl);
		reps.renewableTargets.add(renewableTenderTarget);


		// Renewable Targets - Technology specific
		// ——————————————————————————————————————————————————

		// windonshore target
		TimeSeriesCSVReader nl_nreap_windPGT = new TimeSeriesCSVReader();
		nl_nreap_windPGT.setFilename("/data/NECPPotentials2015.csv");
		nl_nreap_windPGT.setDelimiter(",");
		nl_nreap_windPGT.setStartingYear(0);
		nl_nreap_windPGT.setVariableName("NL_target_Onshore");        
		RenewableTarget renewableTenderTargetNLwindPGT = new RenewableTarget();
		renewableTenderTargetNLwindPGT.setYearlyRenewableTargetTimeSeries(nl_nreap_windPGT);
		renewableTenderTargetNLwindPGT.setTargetTechnologySpecific(true);
		renewableTenderTargetNLwindPGT.setPowerGeneratingTechnology(windOnshore);
		renewableTenderTargetNLwindPGT.setRegulator(regulatorNl);
		reps.renewableTargets.add(renewableTenderTargetNLwindPGT);

		// PV target
		TimeSeriesCSVReader nl_nreap_photovoltaicPGT = new TimeSeriesCSVReader();
		nl_nreap_photovoltaicPGT.setFilename("/data/NECPPotentials2015.csv");
		nl_nreap_photovoltaicPGT.setDelimiter(",");
		nl_nreap_photovoltaicPGT.setStartingYear(0);
		nl_nreap_photovoltaicPGT.setVariableName("NL_target_PV");	  
		RenewableTarget renewableTenderTargetNLphotovoltaicPGT = new RenewableTarget();
		renewableTenderTargetNLphotovoltaicPGT.setYearlyRenewableTargetTimeSeries(nl_nreap_photovoltaicPGT);
		renewableTenderTargetNLphotovoltaicPGT.setTargetTechnologySpecific(true);
		renewableTenderTargetNLphotovoltaicPGT.setPowerGeneratingTechnology(pv);
		renewableTenderTargetNLphotovoltaicPGT.setRegulator(regulatorNl);
		reps.renewableTargets.add(renewableTenderTargetNLphotovoltaicPGT);

		// wind offshore target
		TimeSeriesCSVReader nl_nreap_windoffshorePGT = new TimeSeriesCSVReader();
		nl_nreap_windoffshorePGT.setFilename("/data/NECPPotentials2015.csv");
		nl_nreap_windoffshorePGT.setDelimiter(",");
		nl_nreap_windoffshorePGT.setStartingYear(0);
		nl_nreap_windoffshorePGT.setVariableName("NL_target_Offshore");	  
		RenewableTarget renewableTenderTargetNLwindoffshorePGT = new RenewableTarget();
		renewableTenderTargetNLwindoffshorePGT.setYearlyRenewableTargetTimeSeries(nl_nreap_windoffshorePGT);
		renewableTenderTargetNLwindoffshorePGT.setTargetTechnologySpecific(true);
		renewableTenderTargetNLwindoffshorePGT.setPowerGeneratingTechnology(windOffshore);;
		renewableTenderTargetNLwindoffshorePGT.setRegulator(regulatorNl);
		reps.renewableTargets.add(renewableTenderTargetNLwindoffshorePGT);

		// Technology Specific POTENTIAL LIMITS for Intermittent Technologies
		// ——————————————————————————————————————————————————

		// Wind onshore potential
		TimeSeriesCSVReader nl_windOnshore_limit = new TimeSeriesCSVReader();
		nl_windOnshore_limit.setFilename("/data/NECPPotentialLimits2015.csv");
		nl_windOnshore_limit.setDelimiter(",");
		nl_windOnshore_limit.setStartingYear(0);
		nl_windOnshore_limit.setVariableName("limit_NL_Onshore");

		RenewablePotentialLimit renewablePotentialLimitNLwindOnshore = new RenewablePotentialLimit();
		renewablePotentialLimitNLwindOnshore.setYearlyRenewableTargetTimeSeries(nl_windOnshore_limit);
		renewablePotentialLimitNLwindOnshore.setTargetTechnologySpecific(true);
		renewablePotentialLimitNLwindOnshore.setPowerGeneratingTechnology(windOnshore);
		renewablePotentialLimitNLwindOnshore.setRegulator(regulatorNl);
		reps.renewablePotentialLimits.add(renewablePotentialLimitNLwindOnshore);

		// PV potential       
		TimeSeriesCSVReader nl_photovoltaicPGT_limit = new TimeSeriesCSVReader();
		nl_photovoltaicPGT_limit.setFilename("/data/NECPPotentialLimits2015.csv");
		nl_photovoltaicPGT_limit.setDelimiter(",");
		nl_photovoltaicPGT_limit.setStartingYear(0);
		nl_photovoltaicPGT_limit.setVariableName("limit_NL_PV");

		RenewablePotentialLimit renewablePotentialLimitNLphotovoltaic = new RenewablePotentialLimit();
		renewablePotentialLimitNLphotovoltaic.setYearlyRenewableTargetTimeSeries(nl_photovoltaicPGT_limit);
		renewablePotentialLimitNLphotovoltaic.setTargetTechnologySpecific(true);
		renewablePotentialLimitNLphotovoltaic.setPowerGeneratingTechnology(pv);
		renewablePotentialLimitNLphotovoltaic.setRegulator(regulatorNl);
		reps.renewablePotentialLimits.add(renewablePotentialLimitNLphotovoltaic);

		// Wind offshore potential	  
		TimeSeriesCSVReader nl_windOffshore_limit = new TimeSeriesCSVReader();
		nl_windOffshore_limit.setFilename("/data/NECPPotentialLimits2015.csv");
		nl_windOffshore_limit.setDelimiter(",");
		nl_windOffshore_limit.setStartingYear(0);
		nl_windOffshore_limit.setVariableName("limit_NL_Offshore");

		RenewablePotentialLimit renewablePotentialLimitNLwindOffshore = new RenewablePotentialLimit();
		renewablePotentialLimitNLwindOffshore.setYearlyRenewableTargetTimeSeries(nl_windOffshore_limit);
		renewablePotentialLimitNLwindOffshore.setTargetTechnologySpecific(true);
		renewablePotentialLimitNLwindOffshore.setPowerGeneratingTechnology(windOffshore);
		renewablePotentialLimitNLwindOffshore.setRegulator(regulatorNl);
		reps.renewablePotentialLimits.add(renewablePotentialLimitNLwindOffshore);



		// Policy DE
		// ---------------------------------------------

		Regulator regulatorDe = new Regulator();
		regulatorDe.setName("regulatorGermany");
		regulatorDe.setNumberOfYearsLookingBackToForecastDemand(4);
		regulatorDe.setZone(de);
		reps.regulators.add(regulatorDe);

		// Feed-in Premium details
		RenewableSupportFipScheme premiumDE = new RenewableSupportFipScheme();
		premiumDE.setName("Renewable Support Scheme DE");
		premiumDE.setSupportSchemeDuration(17);
		premiumDE.setEmRevenuePaidExpost(true);
		premiumDE.setFutureSchemeStartTime(5);
		premiumDE.setAvgElectricityPriceBasedPremiumEnabled(false);
		premiumDE.setTechnologySpecificityEnabled(true);
		premiumDE.setCostContainmentMechanismEnabled(true);
		premiumDE.setRegulator(regulatorDe);
		premiumDE.setZone(de);

		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		HashSet<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleDE = new HashSet<>();
		powerGeneratingTechnologiesEligibleDE.add(windOnshore);
		powerGeneratingTechnologiesEligibleDE.add(pv);
		powerGeneratingTechnologiesEligibleDE.add(windOffshore);

		premiumDE.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleDE);


		premiumDE.setFutureSchemePhaseoutTime(schemePhaseOutTimeDE);

		reps.renewableSupportFipSchemes.add(premiumDE);

		// Bias factors

		BiasFactor biasFactorNL1WindDE  = new BiasFactor();
		biasFactorNL1WindDE.setFeedInPremiumBiasFactor(1);
		biasFactorNL1WindDE.setDegressionFactor(0.06);
		biasFactorNL1WindDE.setNode(deNode);
		biasFactorNL1WindDE.setScheme(premiumDE);
		biasFactorNL1WindDE.setTechnology(windOnshore);
		reps.biasFactors.add(biasFactorNL1WindDE);

		BiasFactor biasFactorNL1PVDE  = new BiasFactor();
		biasFactorNL1PVDE.setFeedInPremiumBiasFactor(1);
		biasFactorNL1PVDE.setDegressionFactor(0.06);
		biasFactorNL1PVDE.setNode(deNode);
		biasFactorNL1PVDE.setScheme(premiumDE);
		biasFactorNL1PVDE.setTechnology(pv);
		reps.biasFactors.add(biasFactorNL1PVDE);

		BiasFactor biasFactorNL1WindOffshoreDE  = new BiasFactor();
		biasFactorNL1WindOffshoreDE.setFeedInPremiumBiasFactor(1);
		biasFactorNL1WindOffshoreDE.setDegressionFactor(0.06);
		biasFactorNL1WindOffshoreDE.setNode(deNode);
		biasFactorNL1WindOffshoreDE.setScheme(premiumDE);
		biasFactorNL1WindOffshoreDE.setTechnology(windOffshore);
		reps.biasFactors.add(biasFactorNL1WindOffshoreDE);


		// Renewable Tender Scheme
		// ——————————————————————————————————————————————————
		
		
		long dePhaseOutTick = 10;
		long deTenderDuration = 17;		
		
		
		RenewableSupportSchemeTender renewableSupportSchemeDE1 = new RenewableSupportSchemeTender();
		renewableSupportSchemeDE1.setName("OnshoreTender DE");
		renewableSupportSchemeDE1.setFutureTenderOperationStartTime(2);
		renewableSupportSchemeDE1.setSupportSchemeDuration(deTenderDuration);
		renewableSupportSchemeDE1.setSupportSchemePhaseOutTick(50);
		renewableSupportSchemeDE1.setTechnologySpecificityEnabled(true);
		renewableSupportSchemeDE1.setExpostRevenueCalculation(true);
		renewableSupportSchemeDE1.setRegulator(regulatorDe);
		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleDETender1 = new HashSet<>();
		powerGeneratingTechnologiesEligibleDETender1.add(windOnshore);
		renewableSupportSchemeDE1.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleDETender1);
		reps.renewableSupportSchemeTenders.add(renewableSupportSchemeDE1);
			
		
		RenewableSupportSchemeTender renewableSupportSchemeDE2 = new RenewableSupportSchemeTender();
		renewableSupportSchemeDE2.setName("OffshoreTender DE");
		renewableSupportSchemeDE2.setFutureTenderOperationStartTime(2);
		renewableSupportSchemeDE2.setSupportSchemeDuration(deTenderDuration);
		renewableSupportSchemeDE2.setSupportSchemePhaseOutTick(50);
		renewableSupportSchemeDE2.setTechnologySpecificityEnabled(true);
		renewableSupportSchemeDE2.setExpostRevenueCalculation(true);
		renewableSupportSchemeDE2.setRegulator(regulatorDe);
		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleDETender2 = new HashSet<>();
		powerGeneratingTechnologiesEligibleDETender2.add(windOffshore);
		renewableSupportSchemeDE2.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleDETender2);
		reps.renewableSupportSchemeTenders.add(renewableSupportSchemeDE2);
		
		
		RenewableSupportSchemeTender renewableSupportSchemeDE3 = new RenewableSupportSchemeTender();
		renewableSupportSchemeDE3.setName("PVTender DE");
		renewableSupportSchemeDE3.setFutureTenderOperationStartTime(2);
		renewableSupportSchemeDE3.setSupportSchemeDuration(deTenderDuration);
		renewableSupportSchemeDE3.setSupportSchemePhaseOutTick(10);
		renewableSupportSchemeDE3.setTechnologySpecificityEnabled(true);
		renewableSupportSchemeDE3.setExpostRevenueCalculation(true);
		renewableSupportSchemeDE3.setRegulator(regulatorDe);
		// Define eligible technologies (for those, bias factors and data needs to be available down below)
		Set<PowerGeneratingTechnology> powerGeneratingTechnologiesEligibleDETender3 = new HashSet<>();
		powerGeneratingTechnologiesEligibleDETender3.add(pv);
		renewableSupportSchemeDE3.setPowerGeneratingTechnologiesEligible(powerGeneratingTechnologiesEligibleDETender3);
		reps.renewableSupportSchemeTenders.add(renewableSupportSchemeDE3);
		
		


		// Renewable Targets - Technology neutral
		// ——————————————————————————————————————————————————

		TimeSeriesCSVReader de_nreap = new TimeSeriesCSVReader();
		nl_nreap.setFilename("/data/NECPPotentials2015.csv");
		nl_nreap.setDelimiter(",");
		nl_nreap.setStartingYear(0);
		nl_nreap.setVariableName("DE_target_Total");

		RenewableTarget renewableTenderTargetDE = new RenewableTarget();
		renewableTenderTargetDE.setYearlyRenewableTargetTimeSeries(de_nreap);
		renewableTenderTargetDE.setTargetTechnologySpecific(false);
		renewableTenderTargetDE.setRegulator(regulatorDe);
		reps.renewableTargets.add(renewableTenderTargetDE);


		// Renewable Targets - Technology specific
		// ——————————————————————————————————————————————————

		// windonshore target
		TimeSeriesCSVReader de_necp_windPGT = new TimeSeriesCSVReader();
		de_necp_windPGT.setFilename("/data/NECPPotentials2015.csv");
		de_necp_windPGT.setDelimiter(",");
		de_necp_windPGT.setStartingYear(0);
		de_necp_windPGT.setVariableName("DE_target_Onshore");        
		RenewableTarget renewableTenderTargetDEwindPGT = new RenewableTarget();
		renewableTenderTargetDEwindPGT.setYearlyRenewableTargetTimeSeries(de_necp_windPGT);
		renewableTenderTargetDEwindPGT.setTargetTechnologySpecific(true);
		renewableTenderTargetDEwindPGT.setPowerGeneratingTechnology(windOnshore);
		renewableTenderTargetDEwindPGT.setRegulator(regulatorDe);
		reps.renewableTargets.add(renewableTenderTargetDEwindPGT);

		// PV target
		TimeSeriesCSVReader de_necp_photovoltaicPGT = new TimeSeriesCSVReader();
		de_necp_photovoltaicPGT.setFilename("/data/NECPPotentials2015.csv");
		de_necp_photovoltaicPGT.setDelimiter(",");
		de_necp_photovoltaicPGT.setStartingYear(0);
		de_necp_photovoltaicPGT.setVariableName("DE_target_PV");	  
		RenewableTarget renewableTenderTargetDEphotovoltaicPGT = new RenewableTarget();
		renewableTenderTargetDEphotovoltaicPGT.setYearlyRenewableTargetTimeSeries(de_necp_photovoltaicPGT);
		renewableTenderTargetDEphotovoltaicPGT.setTargetTechnologySpecific(true);
		renewableTenderTargetDEphotovoltaicPGT.setPowerGeneratingTechnology(pv);
		renewableTenderTargetDEphotovoltaicPGT.setRegulator(regulatorDe);
		reps.renewableTargets.add(renewableTenderTargetDEphotovoltaicPGT);

		// wind offshore target
		TimeSeriesCSVReader de_necp_windoffshorePGT = new TimeSeriesCSVReader();
		de_necp_windoffshorePGT.setFilename("/data/NECPPotentials2015.csv");
		de_necp_windoffshorePGT.setDelimiter(",");
		de_necp_windoffshorePGT.setStartingYear(0);
		de_necp_windoffshorePGT.setVariableName("DE_target_Offshore");	  
		RenewableTarget renewableTenderTargetDEwindoffshorePGT = new RenewableTarget();
		renewableTenderTargetDEwindoffshorePGT.setYearlyRenewableTargetTimeSeries(de_necp_windoffshorePGT);
		renewableTenderTargetDEwindoffshorePGT.setTargetTechnologySpecific(true);
		renewableTenderTargetDEwindoffshorePGT.setPowerGeneratingTechnology(windOffshore);;
		renewableTenderTargetDEwindoffshorePGT.setRegulator(regulatorDe);
		reps.renewableTargets.add(renewableTenderTargetDEwindoffshorePGT);

		// Technology Specific POTENTIAL LIMITS for Intermittent Technologies
		// ——————————————————————————————————————————————————

		// Wind onshore potential
		TimeSeriesCSVReader de_windOnshore_limit = new TimeSeriesCSVReader();
		de_windOnshore_limit.setFilename("/data/NECPPotentialLimits2015.csv");
		de_windOnshore_limit.setDelimiter(",");
		de_windOnshore_limit.setStartingYear(0);
		de_windOnshore_limit.setVariableName("limit_DE_Onshore");

		RenewablePotentialLimit renewablePotentialLimitDEwindOnshore = new RenewablePotentialLimit();
		renewablePotentialLimitDEwindOnshore.setYearlyRenewableTargetTimeSeries(de_windOnshore_limit);
		renewablePotentialLimitDEwindOnshore.setTargetTechnologySpecific(true);
		renewablePotentialLimitDEwindOnshore.setPowerGeneratingTechnology(windOnshore);
		renewablePotentialLimitDEwindOnshore.setRegulator(regulatorDe);
		reps.renewablePotentialLimits.add(renewablePotentialLimitDEwindOnshore);

		// PV potential       
		TimeSeriesCSVReader de_photovoltaicPGT_limit = new TimeSeriesCSVReader();
		de_photovoltaicPGT_limit.setFilename("/data/NECPPotentialLimits2015.csv");
		de_photovoltaicPGT_limit.setDelimiter(",");
		de_photovoltaicPGT_limit.setStartingYear(0);
		de_photovoltaicPGT_limit.setVariableName("limit_DE_PV");

		RenewablePotentialLimit renewablePotentialLimitDEphotovoltaic = new RenewablePotentialLimit();
		renewablePotentialLimitDEphotovoltaic.setYearlyRenewableTargetTimeSeries(de_photovoltaicPGT_limit);
		renewablePotentialLimitDEphotovoltaic.setTargetTechnologySpecific(true);
		renewablePotentialLimitDEphotovoltaic.setPowerGeneratingTechnology(pv);
		renewablePotentialLimitDEphotovoltaic.setRegulator(regulatorDe);
		reps.renewablePotentialLimits.add(renewablePotentialLimitDEphotovoltaic);

		// Wind offshore potential	  
		TimeSeriesCSVReader de_windOffshore_limit = new TimeSeriesCSVReader();
		de_windOffshore_limit.setFilename("/data/NECPPotentialLimits2015.csv");
		de_windOffshore_limit.setDelimiter(",");
		de_windOffshore_limit.setStartingYear(0);
		de_windOffshore_limit.setVariableName("limit_DE_Offshore");

		RenewablePotentialLimit renewablePotentialLimitDEwindOffshore = new RenewablePotentialLimit();
		renewablePotentialLimitDEwindOffshore.setYearlyRenewableTargetTimeSeries(de_windOffshore_limit);
		renewablePotentialLimitDEwindOffshore.setTargetTechnologySpecific(true);
		renewablePotentialLimitDEwindOffshore.setPowerGeneratingTechnology(windOffshore);
		renewablePotentialLimitDEwindOffshore.setRegulator(regulatorDe);
		reps.renewablePotentialLimits.add(renewablePotentialLimitDEwindOffshore);



	}
}