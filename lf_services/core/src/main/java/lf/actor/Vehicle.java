// package lf.actor;

// import akka.actor.AbstractActor;
// import akka.actor.Props;

// public class CarActor extends AbstractActor {
//     private CarService carService = new CarService();

//     static Props props() {
//         return Props.create(CarActor.class);
//     }

//     @Override
//     public Receive createReceive() {
//         return receiveBuilder()
//                 .match(CreateCarMessage.class, handleCreateCar())
//                 .match(GetCarMessage.class, handleGetCar())
//                 .build();
//     }

//     private FI.UnitApply<CreateCarMessage> handleCreateCar() {
//         return createCarMessage -> {
//             carService.createCar(createCarMessage.getCar());
//             sender()
//                     .tell(new ActionPerformed(
//                             String.format("Car %s created.", createCarMessage.getCar().getName())), getSelf());
//         };
//     }

//     private FI.UnitApply<GetCarMessage> handleGetCar() {
//         return getCarMessage -> {
//             sender().tell(carService.getCar(getCarMessage.getCarId()), getSelf());
//         };
//     }

// }
