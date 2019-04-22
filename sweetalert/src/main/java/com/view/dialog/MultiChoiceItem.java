package com.view.dialog;

public class MultiChoiceItem {
	private String text;
	private Boolean checked;
	public MultiChoiceItem(String text, Boolean checked) {
		this.text = text;
		this.checked = checked;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Boolean getChecked() {
		return checked;
	}
	public void setChecked(Boolean checked) {
		this.checked = checked;
	}
}
