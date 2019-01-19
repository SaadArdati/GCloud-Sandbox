package me.saad.toraserver;

public class Request {

	private final String torrent;
	private final MegaUser user;

	public Request(String torrent, MegaUser user) {
		this.torrent = torrent;
		this.user = user;
	}

	public String getTorrent() {
		return torrent;
	}

	public MegaUser getUser() {
		return user;
	}
}
