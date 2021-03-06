package com.nineinfosys.businesscalcualtors;

public class businessCalculator {
	private double loanAmount;
    private double intrestRate;
    private int  year;
    private int month;
    private int compounds;
    private double originationfee;
    private double otherfee;
    private double documentationfee;
    double monthlyPayment,annualPayment,totalMonth,totalLoanPayment,totalInterest,totalallfee,totalfee,totalMonths;
	public double getLoanAmount() {
		return loanAmount;
	}
	public void setLoanAmount(double loanAmount) {
		this.loanAmount = loanAmount;
	}
	public double getIntrestRate() {
		return intrestRate;
	}
	public void setIntrestRate(double intrestRate) {
		this.intrestRate = intrestRate;
	}
	public int getYear() {
		return year;
	}
	public void setYear(int year) {
		this.year = year;
	}
	public int getMonth() {
		return month;
	}
	public void setMonth(int month) {
		this.month = month;
	}
	public int getCompounds() {
		return compounds;
	}
	public void setCompounds(int compounds) {
		this.compounds = compounds;
	}
	public double getOriginationfee() {
		return originationfee;
	}
	public void setOriginationfee(double originationfee) {
		this.originationfee = originationfee;
	}
	public double getOtherfee() {
		return otherfee;
	}
	public void setOtherfee(double otherfee) {
		this.otherfee = otherfee;
	}
	public double getDocumentationfee() {
		return documentationfee;
	}
	public void setDocumentationfee(double documentationfee) {
		this.documentationfee = documentationfee;
	}
	
	public businessCalculator(double loanAmount, double intrestRate, int year,
                              int month, double originationfee, double documentationfee, double otherfee) {
		super();
		this.loanAmount = loanAmount;
		this.intrestRate = intrestRate;
		this.year = year;
		this.month = month;
		this.originationfee = originationfee;
		this.documentationfee = documentationfee;
		this.otherfee = otherfee;
	
	}
	public double calculateMonth()
	{
		double YeartoMonth=year*12;
		totalMonths=YeartoMonth+month;
		return totalMonths;
	}
	// Annual Payment
	public double calculateAnnualPayment()
	{
		annualPayment=monthlyPayment*12;

		return annualPayment;
	}
/*
	public double CalculateCompoundInterest()
	{
		r=interestRate/100;
		double m1=(months/12);
		Amount=(loanAmount*(Math.pow((1+(r/noofterms)),(noofterms*m1))));
		return Amount;

	}*/

	  //monthly payment
    public double calculateEMI(){


		double YeartoMonth=year*12;
		totalMonth=YeartoMonth+month;
        double r = intrestRate/1200;
        double r1 = Math.pow(r+1,totalMonth);
        monthlyPayment = (double) ((r+(r/(r1-1))) * loanAmount);	        
        return monthlyPayment;
    }

    //total Payment
    public double calculateTotalPayment()
    {
     totalLoanPayment=monthlyPayment*totalMonth;
    	
    	return totalLoanPayment;
   }


    //total Interest
    public double calculateTotalInterest()
    {
     totalInterest=( totalLoanPayment-loanAmount);
    return totalInterest;
    }
    public double calcualteFee()
    {
    		totalfee=((loanAmount) * (originationfee/100));
    	return totalfee;
    }

	public double totalFee()
	{
		totalfee=((loanAmount) * (originationfee/100));
		 totalallfee=totalfee+documentationfee+otherfee;
		return totalallfee;
	}
    //total interest+fee+other
    public double calculateTotalAll()
    {
		double totalAll=totalInterest+totalallfee;
		return totalAll;
    }
    
    

}
