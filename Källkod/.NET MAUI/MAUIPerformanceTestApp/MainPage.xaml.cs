using System.Diagnostics;

namespace MAUIPerformanceTestApp;

public partial class MainPage : ContentPage
{
    private string longitude, latitude, targetFile;
    private CancellationTokenSource _cancelTokenSource;
    private bool _isCheckingLocation;
    public MainPage()
	{
		InitializeComponent();
    }

    private void OnFibonacciButtonClicked(object sender,EventArgs args) {
        Stopwatch sw = Stopwatch.StartNew();

        Fibonacci(40);
        sw.Stop();
        FibonacciTimeText.Text=$"Fibonacci: {sw.ElapsedMilliseconds}ms";
    }

    private int Fibonacci(int n) {
        if(n<=1) {
            return n;
        } else {
            return Fibonacci(n-1)+Fibonacci(n-2);
        }
    }

    private async void OnGPSButtonClicked(object sender,EventArgs args) {
        PermissionStatus status = await CheckLocationPermission();
        if(status==PermissionStatus.Granted) { 
            try {
                Stopwatch sw = Stopwatch.StartNew();
                _isCheckingLocation=true;

                GeolocationRequest request = new(GeolocationAccuracy.Best,TimeSpan.FromSeconds(10));

                _cancelTokenSource=new CancellationTokenSource();

                Location location = await Geolocation.Default.GetLocationAsync(request,_cancelTokenSource.Token);
                longitude=location.Longitude.ToString().Replace(',','.'); //Replace För att byta , till . för rätt format till URL
                latitude=location.Latitude.ToString().Replace(',','.'); //Replace För att byta , till . för rätt format till URL

                sw.Stop();
                GPSTimeText.Text=$"GPS: {sw.ElapsedMilliseconds}ms";

            } catch(Exception ex) {
                ErrorText.Text = $"{ex.Message}";
            } finally {
                _isCheckingLocation=false;
            }
        } else {
            ErrorText.Text = $"Location permission error: {status}";
        }
    }

    private static async Task<PermissionStatus> CheckLocationPermission() {
        PermissionStatus status = await Permissions.CheckStatusAsync<Permissions.LocationWhenInUse>();

        if(status==PermissionStatus.Granted) {
            return status;
        } else {
            status=await Permissions.RequestAsync<Permissions.LocationWhenInUse>();
        }      
        return status;
    }

    public void CancelRequest() {
        if(_isCheckingLocation&&_cancelTokenSource!=null&&_cancelTokenSource.IsCancellationRequested==false)
            _cancelTokenSource.Cancel();
    }

    private async void OnAPIButtonClicked(object sender,EventArgs args) {
        Stopwatch sw = Stopwatch.StartNew();

        Uri uri = new($"https://api.open-meteo.com/v1/gfs?latitude={latitude}&longitude={longitude}&hourly=temperature_2m,relativehumidity_2m,dewpoint_2m,apparent_temperature,pressure_msl,surface_pressure,precipitation,snowfall,precipitation_probability,weathercode,snow_depth,freezinglevel_height,visibility,cloudcover,cloudcover_low,cloudcover_mid,cloudcover_high,evapotranspiration,et0_fao_evapotranspiration,vapor_pressure_deficit,cape,lifted_index,windspeed_10m,windspeed_80m,winddirection_10m,winddirection_80m,windgusts_10m,soil_temperature_0_to_10cm,soil_temperature_10_to_40cm,soil_temperature_40_to_100cm,soil_temperature_100_to_200cm,soil_moisture_0_to_10cm,soil_moisture_10_to_40cm,soil_moisture_40_to_100cm,soil_moisture_100_to_200cm&past_days=92&forecast_days=16");
        HttpClient client = new();
        var response = await client.GetAsync(uri);
        if(response.IsSuccessStatusCode) 
        {
            string content = await response.Content.ReadAsStringAsync();
            sw.Stop();
            APITimeText.Text=$"API: {sw.ElapsedMilliseconds}ms";

            await SaveToFile(content);
            await ReadFromFile();
        } else {
            sw.Stop();
            ErrorText.Text = $"Error: {response.StatusCode}";
        }
    }

    private async Task SaveToFile(string response) {
        targetFile=FileSystem.Current.AppDataDirectory+"/test.txt";
        Stopwatch sw = Stopwatch.StartNew();
        try {
            FileStream fileStream = new(targetFile,FileMode.OpenOrCreate);
            using StreamWriter streamWriter = new(fileStream);
            await streamWriter.WriteAsync(response);
        
            sw.Stop();
            SaveFileTimeText.Text=$"Save file: {sw.ElapsedMilliseconds}ms";
        } catch(Exception ex) {
            ErrorText.Text = $"{ex.Message}";
            sw.Stop(); 
        }
    }

    private async Task ReadFromFile() {
        Stopwatch sw = Stopwatch.StartNew();
        try {
            using StreamReader reader = new(targetFile);
            string content = await reader.ReadToEndAsync();
            File.Delete(targetFile);

            sw.Stop();
            ReadFileTimeText.Text=$"Read file: {sw.ElapsedMilliseconds}ms";
            File.Delete(targetFile);
        } catch(Exception ex) {
            ErrorText.Text = $"{ex.Message}";
            sw.Stop(); 
        }
    }
}