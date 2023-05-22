package acmfgODMExample;
public class SimpleACObject {
	
	float temperature;
	float humidity;
	String status;
	
	public float getTemperature()
	{
		return temperature;
	}
	
	public void setTemperature(float tempValue)
	{
		 temperature = tempValue;
	}
	
	public void setHumidity(float humidityValue)
	{
		 humidity = humidityValue;
	}
	
	public float  getHumidity()
	{
		 return humidity;
	}
	
	public String getStatus()
	{
		return status;
	}
	
	public void setStatus(String  statusValue)
	{
		status = statusValue;
	}

}
