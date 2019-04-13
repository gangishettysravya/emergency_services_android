package com.example.logindemo;
public class RequestWithId {


        private String citizen;
        private String serviceCategory;
        private double latitude;
        private double longitude;
        private String acceptedBy;
        private String description;
        private String status;
        private String requestId;
        private String expert;


        public RequestWithId(){

        }
    public RequestWithId(String requestId,String citizen,double latitude, double longitude){

        setLatitude(latitude);
        setLongitude(longitude);
        setRequestId(requestId);
        setCitizen(citizen);
    }

      public RequestWithId(String requestId,String citizen,double latitude, double longitude,String Status,String expert){

        setLatitude(latitude);
        setLongitude(longitude);
        setCitizen(citizen);
        setStatus(Status);
        setExpert(expert);
        setRequestId(requestId);

    }
        public RequestWithId(String citizen,String serviceCategory,String description,double latitude, double longitude,String Status,String acceptedBy,String requestId,String expert){
            setCitizen(citizen);
            setServiceCategory(serviceCategory);
            setLatitude(latitude);
            setLongitude(longitude);
            setDescription(description);
            setStatus(Status);
            setAcceptedBy(acceptedBy);
            setRequestId(requestId);
            setExpert(expert);
        }

        public String getExpert(){
            return this.expert;
        }

        public void setExpert(String expert){
            this.expert = expert;
        }

        public String getCitizen() {
            return citizen;
        }

        public void setCitizen(String citizenUsername) {
            this.citizen = citizenUsername;
        }

        public String getServiceCategory() {
            return serviceCategory;
        }

        public void setServiceCategory(String serviceCategory) {
            this.serviceCategory = serviceCategory;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }


        public String getAcceptedBy() {
            return acceptedBy;
        }

        public void setAcceptedBy(String acceptedBy) {
            this.acceptedBy = acceptedBy;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description){
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status){
            this.status = status;
        }

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }
}
