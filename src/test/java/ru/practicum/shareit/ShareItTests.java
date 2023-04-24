package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItTests {
	@Autowired
	private ShareItApp shareItApp;

	@Test
	public void main() {
		shareItApp.main(new String[]{});
	}

	@Test
	void contextLoads() {
	}
}
