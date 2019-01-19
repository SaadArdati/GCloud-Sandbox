package me.saad.toraserver;

public class MegaUser {

	private final String megaUser;
	private final String megaPass;

	public MegaUser(String megaUser, String megaPass) {
		this.megaUser = megaUser;
		this.megaPass = megaPass;
	}

	public String getMegaUser() {
		return megaUser;
	}

	public String getMegaPass() {
		return megaPass;
	}
}
