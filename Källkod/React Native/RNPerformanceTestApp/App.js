import React, { useState } from 'react';
import { View, Text, Button, PermissionsAndroid } from 'react-native';
import Geolocation from 'react-native-geolocation-service';
import { Dirs, FileSystem } from 'react-native-file-access';

const App = () => {
  const [fibonacciTime, setFibonacciTime] = useState(0);
  const [gpsTime, setGPSTime] = useState(0);
  const [apiTime, setAPITime] = useState(0);
  const [saveToFileTime, setSaveToFileTime] = useState(0);
  const [readFromFileTime, setReadFromFileTime] = useState(0);
  const [errorMessage, setErrorMessage] = useState('');
  
  const [latitude,setLatitude] = useState(0);
  const [longitude,setLongitude] = useState(0);

  var path = '';

  const fibonacciButtonPressed = () => {
    const startTime = performance.now();
    fibonacci(40);
    setFibonacciTime(Math.round(performance.now()-startTime));
  }

  const fibonacci = (n) => {
    if (n <= 1) {
      return n;
    } else {
      return fibonacci(n - 1) + fibonacci(n - 2);
    }
  }

  const gpsButtonPressed = async () => {
    const startTime = performance.now();

    if(checkLocationPermissions()) {
      Geolocation.getCurrentPosition(
        position => {
          setLatitude(position.coords.latitude);
          setLongitude(position.coords.longitude);
          setGPSTime(Math.round(performance.now()-startTime));
        },
        (e) => { 
          setErrorMessage(e); 
        },
        { enableHighAccuracy: true, timeout: 15000, maximumAge: 10000 }
      );
    } else {
      setErrorMessage('No location permission');
    }
  };

  const checkLocationPermissions = async () => {
    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.ACCESS_FINE_LOCATION,
        {
          title: 'GPS location',
          message: 'Allow GPS location permission?',
          buttonNeutral: 'Ask Me Later',
          buttonNegative: 'Cancel',
          buttonPositive: 'OK',
        },
      );
      if (granted === PermissionsAndroid.RESULTS.GRANTED) {
        return true;
      } else {
        return false;
      }
    } catch (e) {
      return false;
    }
  }

  const callApi = async () => {
    const startTime = performance.now();
  
    const response = await fetch(`https://api.open-meteo.com/v1/gfs?latitude=${latitude}&longitude=${longitude}&hourly=temperature_2m,relativehumidity_2m,dewpoint_2m,apparent_temperature,pressure_msl,surface_pressure,precipitation,snowfall,precipitation_probability,weathercode,snow_depth,freezinglevel_height,visibility,cloudcover,cloudcover_low,cloudcover_mid,cloudcover_high,evapotranspiration,et0_fao_evapotranspiration,vapor_pressure_deficit,cape,lifted_index,windspeed_10m,windspeed_80m,winddirection_10m,winddirection_80m,windgusts_10m,soil_temperature_0_to_10cm,soil_temperature_10_to_40cm,soil_temperature_40_to_100cm,soil_temperature_100_to_200cm,soil_moisture_0_to_10cm,soil_moisture_10_to_40cm,soil_moisture_40_to_100cm,soil_moisture_100_to_200cm&past_days=92&forecast_days=16`);
    const result = await response.text();
    setAPITime(Math.round(performance.now()-startTime));
    
    await saveToFile(result);
    await readFromFile();  
  }

  const saveToFile = async (result) => {
    path = Dirs.DocumentDir + '/rntest.txt';
    try {
      const startTime = performance.now();

      await FileSystem.writeFile(path,result);
      setSaveToFileTime(Math.round(performance.now()-startTime));
    } catch(e) {
      setErrorMessage(e);
    }
  }
  const readFromFile = async () => {
    try {
      const startTime = performance.now();

      const contents = await FileSystem.readFile(path, 'utf8');
      setReadFromFileTime(Math.round(performance.now()-startTime));
      FileSystem.unlink(path);
    } catch(e) {
      setErrorMessage(e);
    }
  }

  return (
    <View>
      <Button onPress={fibonacciButtonPressed} title="Fibonacci" />
      <Text> {`${fibonacciTime}ms`} </Text>
      <Button onPress={gpsButtonPressed} title="GPS" />
      <Text> {`GPS: ${gpsTime}ms`} </Text>
      <Button onPress={callApi} title="API" />
      <Text> {`API-anrop: ${apiTime}ms`} </Text>
      <Text> {`Spara till fil: ${saveToFileTime}ms`} </Text>
      <Text> {`Läsa från fil: ${readFromFileTime}ms`} </Text>
      <Text> {`${errorMessage}`} </Text>
    </View>
  );
}

export default App;