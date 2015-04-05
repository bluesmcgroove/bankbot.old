package net.bashtech.geobot;

public class QueuedMessage {
	private String target = "";
	private String sender = "";
	private String message = "";
	private String[] args;
	private boolean command = false;
	public QueuedMessage(String target, String sender, String message, String[] args){
		this.setTarget(target);
		this.setSender(sender);
		this.setMessage(message);
		this.setArgs(args);
	}
	public QueuedMessage(String target, String message, boolean command){
		this.target = target;
		this.message = message;
		this.args = null;
		this.sender = null;
		this.setCommand(true);
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String[] getArgs() {
		return args;
	}
	public void setArgs(String[] args) {
		this.args = args;
	}
	public boolean isCommand() {
		return command;
	}
	public void setCommand(boolean command) {
		this.command = command;
	}

}
