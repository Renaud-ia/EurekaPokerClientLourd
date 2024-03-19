package analyzor;

public class TestResources {
    public TestResources() {

    }
    public static void main(String[] args) {
        TestResources testResources = new TestResources();
        System.out.println(TestResources.class.getResource("/hibernate.cfg.xml"));
    }
}
