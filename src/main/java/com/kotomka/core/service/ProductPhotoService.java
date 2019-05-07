package com.kotomka.core.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.*;

@Service
public class ProductPhotoService {

    public boolean forUploadProductPhoto(InputStream uploadedProductPhotoIs, String productPhotosDirPath, String productIdPhotoFnWithExt) {
        try {
            deleteProductPhoto(productPhotosDirPath, productIdPhotoFnWithExt);
            saveProductPhoto(uploadedProductPhotoIs, productPhotosDirPath, productIdPhotoFnWithExt);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public byte[] forDownloadProductPhoto(String productPhotosDirPath, String productIdPhotoFnWithExt) {
        try {
            File productPhotoFile = searchFilenameByBase(productPhotosDirPath, productIdPhotoFnWithExt);
            if (productPhotoFile != null) {
                try (InputStream fileIs = new FileInputStream(productPhotoFile)) {
                    return IOUtils.toByteArray(fileIs);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File searchFilenameByBase(String photosDirPath, String fnProductId) throws FileNotFoundException {
        File photosDir = new File(photosDirPath);
        if (photosDir.exists() && photosDir.isDirectory()) {
            String[] fileNames = photosDir.list((dir, name)
                    -> FilenameUtils.getBaseName(name)
                    .equals(FilenameUtils.getBaseName(fnProductId)));
            if (fileNames != null && fileNames.length == 1) {
                File photoFile = new File(FilenameUtils.concat(photosDirPath, fileNames[0]));
                if (photoFile.exists() && photoFile.isFile()) {
                    return photoFile;
                }
            }
            return null;
        }
        throw new FileNotFoundException("Directory " + photosDirPath + " is not exists");
    }

    public void deleteProductPhoto(String photosDirPath, String productIdPhotoFnWithExt) throws FileNotFoundException {
        File productPhotoFile = searchFilenameByBase(photosDirPath, productIdPhotoFnWithExt);
        if (productPhotoFile != null) {
            productPhotoFile.delete();
        }
    }

    public void saveProductPhoto(InputStream fileInputStream, String photosDirPath,
                                 String productIdPhotoFnWithExt) throws IOException {
        File photosDir = new File(photosDirPath);
        if (photosDir.exists() && photosDir.isDirectory()) {
            String photoFilePath = FilenameUtils.concat(photosDirPath, productIdPhotoFnWithExt);
            try (OutputStream output = new FileOutputStream(photoFilePath)) {
                IOUtils.copy(fileInputStream, output);
            } catch (Exception e) {
                throw new IOException("Error by saving product photo file " + productIdPhotoFnWithExt);
            } finally {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        } else {
            throw new FileNotFoundException("Directory " + photosDirPath + " is not exists");
        }
    }
}