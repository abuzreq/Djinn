import java.util.Random;

public class Gene  
{
	private double low = 0,high = 1,value;
	public Gene(Random rand, double s, double e) 
	{
		setLow(s);
		setHigh(e);
		value = getLow() + (getHigh() - getLow()) * rand.nextDouble();
		assert(value >= s && value <= e);
	}	
	private Gene(double s, double e) 
	{
		setLow(s);
		setHigh(e);
		value = (s+e)/2;
	}

	public double getValue()
	{
		return value;
	}
	final int toStringNumDecimals = 3;
	public double getRange()
	{
		return high - low;
	}
	public String toString()
	{
		String str = Double.toString(value);
		int first = str.indexOf(".")+1;
		return str.substring(first,first+toStringNumDecimals);
	}
	public Gene clone()
	{
		Gene newVar = new Gene(getLow(),getHigh());
		newVar.value = this.value;
		return newVar;		
	}
	public void update(double del) 
	{
		this.value += del;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	
}
