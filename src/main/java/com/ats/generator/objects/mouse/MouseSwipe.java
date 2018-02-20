package com.ats.generator.objects.mouse;

import com.ats.generator.objects.MouseDirectionData;

public class MouseSwipe extends Mouse {

	private int hdir;
	private int vdir;
	
	public MouseSwipe(int hdir, int vdir) {
		setHdir(hdir);
		setVdir(vdir);
	}

	public MouseSwipe(int hdir, int vdir, MouseDirectionData hpos, MouseDirectionData vpos) {
		super(hpos, vpos);
		setHdir(hdir);
		setVdir(vdir);
	}
	
	public int getHdir() {
		return hdir;
	}

	public void setHdir(int hdir) {
		this.hdir = hdir;
	}

	public int getVdir() {
		return vdir;
	}

	public void setVdir(int vdir) {
		this.vdir = vdir;
	}
}
