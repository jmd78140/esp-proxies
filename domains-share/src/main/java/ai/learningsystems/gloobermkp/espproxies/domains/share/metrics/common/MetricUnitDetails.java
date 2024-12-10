package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common;


public class MetricUnitDetails
{
    private String shortForm;
    private String longForm;
    private String description;
    
   
    public  MetricUnitDetails(final String shortForm, 
            final String longForm,
            final String description) 
    {
        this.shortForm = shortForm;
        this.longForm = longForm;
        this.description = description;
    }


    
    public String getShortForm()
    {
    
        return shortForm;
    }


    
    public void setShortForm(String shortForm)
    {
    
        this.shortForm = shortForm;
    }


    
    public String getLongForm()
    {
    
        return longForm;
    }


    
    public void setLongForm(String longForm)
    {
    
        this.longForm = longForm;
    }


    
    public String getDescription()
    {
    
        return description;
    }


    
    public void setDescription(String description)
    {
    
        this.description = description;
    }
    
    
}
