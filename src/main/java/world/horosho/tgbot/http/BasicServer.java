package world.horosho.tgbot.http;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import world.horosho.tgbot.database.controller.DatabaseController;
import world.horosho.tgbot.http.telegram.DataBroadcaster;
import world.horosho.tgbot.manager.BotMngrProperties;
import world.horosho.tgbot.services.LoggerService;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;

import static world.horosho.tgbot.http.telegram.DataBroadcaster.joinedAdminList;

public class BasicServer {
    private HttpServer server;
    private DataBroadcaster dataBroadcaster;

    public BasicServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        HttpContext ctx = server.createContext("/data");
        ctx.setHandler(this::handleRequest);
        server.start();
        dataBroadcaster = new DataBroadcaster(new BotMngrProperties());
        System.out.println("Server started at port " + port);
    }

    private void handleRequest(HttpExchange req){
        try{
            if (req.getRequestMethod().equals("POST")) {
                InputStreamReader reader = new InputStreamReader(req.getRequestBody(), StandardCharsets.UTF_8);

                BufferedReader bufferedReader = new BufferedReader(reader);
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    body.append(line);
                }

                ObjectMapper mapper = new ObjectMapper();
                WebAppModel dto = mapper.readValue(body.toString(), WebAppModel.class);
                System.out.println(dto);
                System.out.println(dto.getReturned());

                String mentions = joinedAdminList(DatabaseController.getAdminsForCompanyMention(dto.getCompany()));
                System.out.println("MENTIONS: " + mentions);


                switch (dto.getState()){
                    case "DIRECT_BROADCAST" -> dataBroadcaster.directBroadcast(dto);
                    case "Yeni" -> {
                        if (dto.getReturned()!= null && dto.getReturned()){
                            dataBroadcaster.groupBroadcast(dto,
                        "❌❌Kategoriya dəyişən sənəd-hesabat:\n <s>"+dto.getExState()+"</s> >>>>> <b>"+dto.getState()+"</b>\n     Yükləyən əməkdaş: "+dto.getAuthor()+"\n    Müəssisə: "+dto.getCorporation()+"\n    Statusu dəyişən admin: "+dto.getMoveIssuer()+"\n" + mentions);

                        }else{
                            dataBroadcaster
                                    .groupBroadcast(dto,
                                    "\uD83D\uDCDDYeni yüklənən sənəd-hesabat:\n   Yükləyən əməkdaş: " + dto.getAuthor()  + "\n   Müəssisə: " + dto.getCorporation() + "\n" + mentions);
                        }
                    }
                    case "İcrada olan" -> {
                        if (dto.getReturned()!= null && dto.getReturned()){
                            dataBroadcaster.groupBroadcast(dto,
                                    "❌❌Kategoriya dəyişən sənəd-hesabat:\n <s>"+dto.getExState()+"</s> >>>>> <b>"+dto.getState()+"</b>\n     Yükləyən əməkdaş: "+dto.getAuthor()+"\n    Müəssisə: "+dto.getCorporation()+"\n    Statusu dəyişən admin: "+dto.getMoveIssuer()+"\n" + (dto.getLink() != null ? DataBroadcaster.generateLink(dto.getLink()) : "") + "\n" + mentions);
                        }else{
                            dataBroadcaster
                                .groupBroadcast(dto,
                                "‼️Kategoriya dəyişən sənəd-hesabat:\n <s>Yeni</s> >>>>>  <b>İcrada olan</b>\n    Yükləyən əməkdaş: "+dto.getAuthor()+"\n    Müəssisə: "+dto.getCorporation()+"\n    Kategoriya: "+dto.getCategory()+"\n    Təsdiq eliyən baş admin: "+dto.getHeadAdminName()+"\n"+ (dto.getLink() != null ? DataBroadcaster.generateLink(dto.getLink()) : "") + "\n" + mentions);
                        }
                    }
                    case "Göndərilmişlər" ->
                        dataBroadcaster
                            .groupBroadcast(dto,
                            "️⁉️ Kategoriya dəyişən sənəd-hesabat:\n <s>İcrada olan</s> >>>>>  <b>Göndərilmişlər</b>\n    Yükləyən əməkdaş: "+dto.getAuthor()+"\n    Müəssisə: "+dto.getCorporation()+"\n    Kategoriya: "+dto.getCategory()+"\n    Təsdiq eliyən baş admin: "+dto.getHeadAdminName()+"\n"+ (dto.getLink() != null ? DataBroadcaster.generateLink(dto.getLink()) : "") +"\n    'Arxiv' statusuna keçirilməsi qədər "+dto.getTimeLeft()+" iş gün qalıb" + "\n" + mentions);

                    case "Arxiv" -> {
                        if (dto.getFlag() != null && dto.getFlag()){
                            dataBroadcaster
                                .groupBroadcast(dto,
                                "✅Kategoriya dəyişən sənəd-hesabat:\n <s>Göndərilmişlər</s> >>>>>  <b>Arxiv</b>\n     Yükləyən əməkdaş: "+dto.getAuthor()+"\n    Müəssisə: "+dto.getCorporation()+"\n    Kategoriya: "+dto.getCategory()+"\n    Təsdiq eliyən baş admin: "+dto.getHeadAdminName()+"\n" + (dto.getLink() != null ? DataBroadcaster.generateLink(dto.getLink()) : "") + "\n" + mentions);
                        }else {
                            dataBroadcaster
                                .groupBroadcast(dto,"✅Kategoriya dəyişən sənəd-hesabat:\n <s>İcrada olan</s> >>>>>  <b>Arxiv</b>\n     Yükləyən əməkdaş: "+dto.getAuthor()+"\n    Müəssisə: "+dto.getCorporation()+"\n    Kategoriya: "+dto.getCategory()+"\n    Təsdiq eliyən baş admin: "+dto.getHeadAdminName()+"\n"+ (dto.getLink() != null ? DataBroadcaster.generateLink(dto.getLink()) : "") + "\n" + mentions);
                        }
                    }

                    case "Delete" -> {
                        if (dto.getAdminDelete() != null && dto.getAdminDelete()){
                            dataBroadcaster.groupBroadcast(
                                dto,
                                "\uD83D\uDDD1Silinmiş sənəd-hesabat:\n    Müəssisə: "+dto.getCorporation()+"\n    Sənəd-hesabat admin tərəfindən silinib: " + dto.getDeleteIssuer() + "\n" + mentions
                            );
                        }else{
                            dataBroadcaster.groupBroadcast(
                                dto,
                                "\uD83D\uDDD1Silinmiş sənəd-hesabat:\n    Müəssisə: "+dto.getCorporation()+"\n    Sənəd-hesabat yükləyən istifadəçi tərəfindən silinib: " + dto.getDeleteIssuer() + "\n" + mentions
                            );
                        }
                    }
                }

                req.sendResponseHeaders(200, 2);
                OutputStream os = req.getResponseBody();
                os.write("OK".getBytes(StandardCharsets.UTF_8));
                os.close();
            }else{
                String badRequest = "Bad request";
                req.sendResponseHeaders(400, badRequest.length());
                OutputStream os = req.getResponseBody();
                os.write(badRequest.getBytes(StandardCharsets.UTF_8));
                os.close();

            }
        } catch (IOException e) {
            LoggerService.log("Could not handle a HTTP request", Level.WARNING);
            throw new RuntimeException(e);
        }
    }

}
