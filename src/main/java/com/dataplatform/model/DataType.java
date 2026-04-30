package com.dataplatform.model;

public enum DataType {
    VEHICLE("Vehicle", "vin,make,model,year,trim,color,fuelType,transmission,engineSize,bodyStyle,dealerCode,status"),
    DEALER("Dealer", "code,name,address,city,state,zipCode,phone,email,website,status"),
    WARRANTY("Warranty", "warrantyNumber,warrantyType,startDate,endDate,mileageLimit,coverage,deductible,vin,provider,status"),
    FLEET("Fleet", "fleetCode,fleetName,company,address,city,state,zipCode,contactPerson,contactPhone,contactEmail,vehicleCount,status"),
    SERVICE_RECORD("Service Record", "serviceNumber,serviceType,serviceDate,mileage,description,cost,vin,dealerCode,technician,status");

    private final String displayName;
    private final String csvSchema;

    DataType(String displayName, String csvSchema) {
        this.displayName = displayName;
        this.csvSchema = csvSchema;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCsvSchema() {
        return csvSchema;
    }

    public String[] getSchemaFields() {
        return csvSchema.split(",");
    }
}
