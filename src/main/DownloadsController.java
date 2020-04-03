package main;


public class DownloadsController extends Controller {

    @Override
    protected void init(){
        System.out.println("Init method is run on downloads controller");
    }

    public void createWindow()  {
        super.createWindow("Creating downloads window!", "/downloads.fxml");
    }

}
