package Biocipher.demo;

import com.jcraft.jsch.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Objects;
import java.util.Vector;

@SpringBootApplication
public class ServerConnApplication {


	public static void main(String[] args)
	{
		fileTransFromServerToAnotherServer("<source username>","<source password>","<source ip>",00,"<source path>");
	}

	public static void fileTransFromServerToAnotherServer(String userName, String password, String ip, int port, String sourceDirPath) {
		try {
			SftpATTRS attrs = null;


/**
 *  SOURCE SERVER  DETAILS
 *
 */			JSch jsch = new JSch();
			Session sourcesession = jsch.getSession(userName, ip, port);
			sourcesession.setPassword(password);
			sourcesession.setConfig("StrictHostKeyChecking", "no");
			sourcesession.connect();
			System.out.println("SOURCE SERVER CONNECTED");

			/**
			 *  Destination SERVER  DETAILS
			 */
			JSch jsch1 = new JSch();
			Session destinationServer = jsch1.getSession("<place with destination username>", "<place with destination host>", 00);
			destinationServer.setPassword("<Your Destination server password>");
			destinationServer.setConfig("StrictHostKeyChecking", "no");
			destinationServer.connect();
			System.out.println("DESTINATION SERVER CONNECTED");

			/*
			 * Sftp CHANNEL server for File Transfer
			 */

			ChannelSftp channelSftp = (ChannelSftp) sourcesession.openChannel("sftp");
			channelSftp.connect();
			channelSftp.cd(sourceDirPath);
			String currentDirectory = channelSftp.pwd();

			ChannelSftp channelSftpB = (ChannelSftp) destinationServer.openChannel("sftp");
			channelSftpB.connect();
			try {
				attrs = channelSftpB.stat("/usr/ServerLogs"); // checking if folder alredy created or not
			} catch (Exception e) {
				System.out.println("/usr/ServerLogs" + "  ==== not found" + e.getMessage());
			}

			if (attrs != null) {
				System.out.println("Directory exists IsDir=" + attrs.isDir());
			} else {
				channelSftpB.mkdir("/usr/ServerLogs");  // creating folder as per your requirement you can change
			}
			channelSftpB.cd("/usr/ServerLogs");  // changing directory to new one

			String currentDirectoryD = channelSftpB.pwd();  // getting address of current work directory


			SftpATTRS dirName = null;
			try {
				dirName = channelSftpB.stat(currentDirectoryD + "/" + "SERVER -" + sourcesession.getHost()); // checking if folder alredy created or not
			} catch (Exception e) {
				System.out.println("/usr/ServerLogs" + "  ==== not found");
			}

			if (dirName != null) {
				System.out.println("Directory exists IsDir=" + dirName.isDir());  // ignoring if dir already exists
			} else {
				channelSftpB.mkdir(currentDirectoryD + "/" + "SERVER -" + sourcesession.getHost()); // creating folder as per your requirement you can change
			}

			channelSftpB.cd(currentDirectoryD + "/" + "SERVER -" + sourcesession.getHost()); // changing directory to new one

			currentDirectoryD = channelSftpB.pwd(); // getting address of current work directory

			Vector filelist = channelSftp.ls(sourceDirPath);  // gettting list of files


			/*
			 * FILE TRANSFER METHOD FROM SOURCE SERVER TO DESTINATION SERVER
			 */
			int i = 0;
			for (Object o : filelist) {

				ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) o;

				if (Objects.equals(entry.getFilename(), ".") || Objects.equals(entry.getFilename(), "..")) {
					System.out.println(i + "/" + filelist.size() + "==" + "Skipped");
				} else {

					System.out.println(i + "/" + filelist.size() + "==" + entry.getFilename());
					/*
					 * FILE TRANSFER METHOD FROM SOURCE SERVER TO DESTINATION SERVER
					 */
					InputStream srcInputStream = channelSftp.get(currentDirectory + "/" + entry.getFilename());
					SftpProgressMonitor SftpProgressMonitor = null;
					channelSftpB.put(srcInputStream, currentDirectoryD + "/" + entry.getFilename());
					System.out.println("Transfer has been completed");

					/*
					 * REMOVING FILE FROM SERVER AFTER UPLOAD
					 */

					channelSftp.rm(currentDirectory + "/" + entry.getFilename());
					System.out.println(currentDirectory + "/" + entry.getFilename() + "======" + "FILE DELETED SUCCESSFULLY");


				}
				i++;
			}

/**
		Closing all connections and sessions
 */
			channelSftp.disconnect();
			channelSftpB.disconnect();
			sourcesession.disconnect();
			destinationServer.disconnect();
			System.out.println("DONE");

		} catch (JSchException | SftpException e) {
			throw new RuntimeException(e.getMessage());
		}


	}
}