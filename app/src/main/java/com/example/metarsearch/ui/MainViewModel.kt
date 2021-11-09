package com.example.metarsearch.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.net.ftp.FTPClient
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.net.URLConnection

private const val TAG = "MainViewModel"

class MainViewModel : ViewModel() {


    private lateinit var mFTPClient: FTPClient
    var rawTxt: MutableLiveData<String> = MutableLiveData("Only German Stations can be searched!")
    var decodedTxt: MutableLiveData<String> =
        MutableLiveData("Only German Stations can be searched!")


    private fun setUpFtpConnection() {
        mFTPClient = FTPClient()
        mFTPClient.connect("tgftp.nws.noaa.gov",21)
        mFTPClient.login("anonymous", "nobody")
        mFTPClient.enterLocalPassiveMode()
        Log.d(TAG, "setUpFtpConnection: Connection status ${mFTPClient.isConnected}")
    }

    fun connectToFtp() {
        viewModelScope.launch(Dispatchers.IO) {
            setUpFtpConnection()
        }
    }


    fun search(stationIdOrName: String) {
        /*viewModelScope.launch {
            searchForStationInDirectory(stationIdOrName)
        }*/

        viewModelScope.launch(Dispatchers.IO) {
            val urlStrRaw = "https://tgftp.nws.noaa.gov/data/observations/metar/stations/"
            val urlStrDecoded = "https://tgftp.nws.noaa.gov/data/observations/metar/decoded/"
            rawTxt.postValue(establishFtpConnection("$urlStrRaw$stationIdOrName.TXT"))
                decodedTxt.postValue(establishFtpConnection("$urlStrDecoded$stationIdOrName.TXT"))
        }

    }

    /*private fun searchForStationInDirectory(stationIdOrName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!mFTPClient.isConnected) {
                    val a = async { setUpFtpConnection() }
                    val res = a.await()
                }
                val fileNmaeToSearch = "$stationIdOrName.TXT"
                if (mFTPClient.changeWorkingDirectory(FTP_STATION_DIR)) {
                    var fileNames = mFTPClient.listNames()
                    var ftpFile = mFTPClient.retrieveFileStream(FTP_STATION_DIR + fileNmaeToSearch)
                    if (ftpFile != null) {

                        var isr = InputStreamReader(ftpFile, "UTF8")
                        rawTxt.postValue(isr.readText())
                        isr.close()
                        
                        if (mFTPClient.isConnected && mFTPClient.isAvailable){
                            ftpFile = mFTPClient.retrieveFileStream(FTP_DECODED_DIR + fileNmaeToSearch)
                            if (ftpFile!=null){
                                isr = InputStreamReader(ftpFile, "UTF8")
                                decodedTxt.postValue(isr.readText())
                            }
                        }else {
                            Log.d(TAG, "searchForStationInDirectory: remote not available")
                        }
                        
                        


                    } else {
                        if (fileNames != null && fileNames.isNotEmpty() && fileNames.contains(
                                fileNmaeToSearch
                            )
                        ) {
                            var inStream: InputStream =
                                mFTPClient.retrieveFileStream(fileNmaeToSearch)
                            var isr = InputStreamReader(inStream, "UTF8")
                            rawTxt.postValue(isr.readText())

                            if (mFTPClient.changeWorkingDirectory(FTP_DECODED_DIR)) {
                                fileNames = mFTPClient.listNames()
                                if (fileNames != null && fileNames.isNotEmpty() && fileNames.contains(
                                        fileNmaeToSearch
                                    )
                                ) {
                                    inStream = mFTPClient.retrieveFileStream(fileNmaeToSearch)
                                    isr = InputStreamReader(inStream, "UTF8")
                                    decodedTxt.postValue(isr.readText())
                                } else {
                                    decodedTxt.postValue("Not found! in ${mFTPClient.listNames().size}")
                                    Log.d(
                                        TAG,
                                        "searchForStationInDirectory: No found Decode in ${mFTPClient.listNames().size}"
                                    )
                                }
                            } else {
                                decodedTxt.postValue("Not found! in ${mFTPClient.listNames().size}")
                            }


                        } else {
                            rawTxt.postValue("Not found! in ${mFTPClient.listNames().size}")
                            decodedTxt.postValue("Not found! in ${mFTPClient.listNames().size}")
                            Log.d(
                                TAG,
                                "searchForStationInDirectory: No found! in ${mFTPClient.listNames().size}"
                            )
                        }
                    }

                }

            } catch (e: Exception) {
                rawTxt.postValue("Error getting information")
                decodedTxt.postValue("Error getting information")
                Log.d(TAG, "searchForStationInDirectory: ${e.localizedMessage}")
                e.printStackTrace()
            }
        }


    }*/

    private suspend fun establishFtpConnection(fullUrlPathName:String) :String{
        return try {
            val url = URL(fullUrlPathName)
            val cn: URLConnection = url.openConnection()
           /* cn.setRequestProperty(
                "Authorization",
                "Basic " + Base64.encodeToString("anonymous:a@b.c".toByteArray(), Base64.DEFAULT)
            )*/

            val `is`: InputStream = cn.getInputStream()
            val insReader = InputStreamReader(`is`,"UTF-8")
            insReader.readText()
        }catch (e:java.lang.Exception){
            "Station doesn't exists"
        }

    }

}