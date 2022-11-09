package service.message;

public class ApplicationRequestDeadline implements MySerialisable {
    private long id;

    public ApplicationRequestDeadline(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

}