export const sampleCsvByType = {
  VEHICLE: `vin,make,model,year,trim,color,fuelType,transmission,engineSize,bodyStyle,dealerCode,status
SALAB2BN1HH123456,Jaguar,F-Pace,2024,R-Dynamic,Blue,Petrol,Automatic,2.0,SUV,DLR001,Active`,
  DEALER: `code,name,address,city,state,zipCode,phone,email,website,status
DLR001,North Wales Jaguar,Parc Menai,Bangor,Gwynedd,LL57 4BN,01248 000000,ops@nwjaguar.example,nwjaguar.example,Active`,
  WARRANTY: `warrantyNumber,warrantyType,startDate,endDate,mileageLimit,coverage,deductible,vin,provider,status
WRN-1001,Extended,2026-01-01,2029-01-01,60000,Powertrain,250,SALAB2BN1HH123456,Manufacturer,Active`,
  FLEET: `fleetCode,fleetName,company,address,city,state,zipCode,contactPerson,contactPhone,contactEmail,vehicleCount,status
FLT001,Executive Vehicles,Example Logistics,Parc Menai,Bangor,Gwynedd,LL57 4BN,A Morgan,01248 111111,fleet@example.com,24,Active`,
  SERVICE_RECORD: `serviceNumber,serviceType,serviceDate,mileage,description,cost,vin,dealerCode,technician,status
SRV-1001,Oil Change,2026-02-14,12000,Scheduled service,189.99,SALAB2BN1HH123456,DLR001,A Morgan,Completed`,
};

export const endpointByType = {
  VEHICLE: "/api/records/vehicles",
  DEALER: "/api/records/dealers",
  WARRANTY: "/api/records/warranties",
  FLEET: "/api/records/fleets",
  SERVICE_RECORD: "/api/records/services",
};

export const recordColumnsByType = {
  DEALER: ["code", "name", "city", "status"],
  WARRANTY: ["warrantyNumber", "warrantyType", "provider", "status"],
  FLEET: ["fleetCode", "fleetName", "company", "status"],
  SERVICE_RECORD: ["serviceNumber", "serviceType", "technician", "status"],
  VEHICLE: ["vin", "make", "model", "status"],
};
