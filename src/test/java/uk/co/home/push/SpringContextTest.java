package uk.co.home.push;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.co.home.push.Application;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class })
public class SpringContextTest {
    @Autowired
    ApplicationContext ctx;

    @Test
    public void wiring() throws Exception {
        assertNotNull(ctx.getBean(ObjectMapper.class));
    }
}
