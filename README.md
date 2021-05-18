# SMAPackageManager

Specific library to manage download and unzip package.
Zip package is downloaded to public application folder (Android/package_name/zip_folder) then unzip in private application folder (packages).
This library use the DownloadManager and handle many states :

- when you lose connexion, download will be paused and restart immediately after connexion restarted.
- when you kill the app, the download manager will finish his task 

To use this library you must have those permissions in your application manifest : 

    :::xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />

This library has some cool dependencies that help managing files :

    :::javascript
    compile 'commons-io:commons-io:2.4'


# Import

    :::javascript
    // project gradle
    allprojects {
        repositories {
            jcenter()
            maven { url 'https://dl.bintray.com/smartapps/maven' }
        }
    }
    
    // module gradle
    compile 'fr.smartapps.library:lib_sma-packagemanager:1.0.11'

# Implement

Here is the main method to download a package. You must handle each states from the callback this way :

    :::java
    SMAPackageManager packageManager = SMAPackageManager.getInstance(context);
    packageManager.downloadUnzipPackage(url, packageName, new SMAPackageListener() {
                        @Override
                        public void onProgress(int progressOn100, File packageFile, PackageStatus status) {
                            switch (status) {
                                case DOWNLOAD_PAUSED:
                                    textView.setText("Waiting for connexion ...");
                                    break;
                                case DOWNLOAD_PENDING:
                                    textView.setText("Connecting ...");
                                    break;
                                case DOWNLOAD_RUNNING:
                                    textView.setText("Download progress : " + progressOn100 + " / 100");
                                    break;
                                case DOWNLOAD_SUCCESSFUL:
                                    textView.setText("Your file has been successfully downloaded");
                                    break;
                                case DOWNLOAD_FAILED:
                                    textView.setText("Download failed, please try again");
                                    break;

                                case UNZIP_RUNNING:
                                    textView.setText("Unzip progress : " + progressOn100 + " / 100");
                                    break;
                                case UNZIP_SUCCESSFUL:
                                    textView.setText("Your file has been successfully unzip");
                                    break;
                                case UNZIP_FAILED:
                                    textView.setText("Unzip failed, please try again");
                                    break;
                            }
                        }
                    });

SMAPackageManager offers more methods to manage packages...

## Get absolute path to main folders

Public **zip folder** contains every zip you have downloaded.  
This folder should be regularly cleared except if you want to keep zip files.    
Private **package folder** contains every packages you have unzipped.  

    :::java
    // return absolute path to zip folder
    packageManager.getZipFolder()

    // return absolute path to package folder
    packageManager.getPackageFolder()

## Clear main folders

    :::java
    // delete all zip in zip folder
    packageManager.clearZipFolder();

    // delete all packages in package folder
    packageManager.deleteAllPackages();

    // delete 1 package in package folder
    packageManager.deletePackage(String packageName);

    // delete all packages in package folder except "package_1" and "package_2"
    packageManager.deleteAllPackages("package_1", "package_2");

## List files in main folders

When you list each main folder, it will delete every file that is not a package (folder) or a zip (*.zip)

    :::java
    // return list of filename (files contains at root of zip folder)
    packageManager.zipList();    

    // return list of filename (files contains at root of package folder)
    packageManager.packageList();

## Check if package exist

    :::java
    // return true if exist, else return false
    packageManager.containsPackage(packageFilename);

## Get main folders paths

    :::java
    // return file zip folder
    packageManager.getZipFolder();

    // return file package folder
    packageManager.getPackageFolder();

## HTTP Calls

    :::java
    // simple get call on URL
    packageManager.startGETRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "response : " + response.body().string());
            }
        });

    // simple post call with parameters on URL
    HashMap<String, String> parameters = new HashMap();
    parameters.put("username", user.username);
    parameters.put("password", user.password);

    packageManager.startPOSTRequest(url, parameters, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e(TAG, "is successfully posted : " + response.isSuccessful());
            }
        });