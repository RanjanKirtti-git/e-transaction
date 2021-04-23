package com.kirtti.banking.controller;

import com.kirtti.banking.entity.Customers;
import com.kirtti.banking.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.time.LocalTime;
import java.util.Random;

@Controller
public class MainController {
    private CustomerRepository cr;
    private JavaMailSender ms;
    @Autowired
    public MainController(CustomerRepository cr, JavaMailSender ms) {
        this.cr = cr;
        this.ms = ms;
    }

    @RequestMapping("/")
    String index1(Model m)
    {
       m.addAttribute("cust",new Customers());
        return "index";
    }
    @PostMapping("indexsub")
   String IndexSub(@ModelAttribute("cust") Customers c, PrintWriter pw, HttpSession hs,Model m)
    {
        if(cr.existsById(c.getUserId()))
        {
            if(c.getPassword().equals(cr.getOne(c.getUserId()).getPassword()))
            {
                hs.setAttribute("sessionId",c.getUserId());
                Customers one = cr.getOne(c.getUserId());
                String na=one.getFirstName()+"  "+one.getLastName();
                String name="\t"+na.toUpperCase();
                //for wishes
                LocalTime lt=LocalTime.now();
                int hr=lt.getHour();
                if(hr>5 && hr<12)
                {
                    m.addAttribute("wish","Good Morning"+"\t\t"+name);
                }
                else if (hr>=12 && hr<17)
                {
                    m.addAttribute("wish","Good Afternoon"+"\t\t"+name);
                }
                else if(hr>=17 && hr<20)
                {
                    m.addAttribute("wish","Good Evening"+"\t"+name);
                }
                else {
                    m.addAttribute("wish","Good Night"+"\t"+name);
                }


                m.addAttribute("accdata",one);
                return "cushome";
            }else {
                m.addAttribute("perr","Sorry, Incorrect Password");
                return "index";
            }
        }else {
            m.addAttribute("uerr","Sorry,Incorrect UserId");
            return "index";
        }
    }
    @GetMapping("home")
    String Home(HttpSession hs,Model m)

    {
        String tempid= (String) hs.getAttribute("sessionId");
        Customers one = cr.getOne(tempid);
        m.addAttribute("accdata",one);
        return "cushome";
    }
    @GetMapping("newacc")
    String createAcc(Model m)
    {
        m.addAttribute("cust1",new Customers());
        return "acc";
    }
    @PostMapping("accsub")
    String accSub(@ModelAttribute("cust1") Customers c, @RequestParam("cimg")MultipartFile mf, PrintWriter pw,Model m) throws Exception {
        if(mf.isEmpty()) {
            //pw.println("Error in image uploading \n Try Again.");
            m.addAttribute("errsub","Some Error arise in Data Submit ,Please Try Again..");
            return "acc";
        }
        else {
            Random rr=new Random();
            String ran= String.valueOf(rr.nextInt(9999));
            String n=c.getFirstName();
            String fn=n.toLowerCase();
            String uidd=fn+ran;

            String ss[]=mf.getOriginalFilename().split("\\.");
            String ex=ss[ss.length-1];
            String na=uidd+"."+ex;
            FileOutputStream fos=new FileOutputStream("img"+ File.separator+na);
            fos.write(mf.getBytes());
            c.setCimage(na);

            String mai=c.getEmail();

            SimpleMailMessage sm=new SimpleMailMessage();
            sm.setTo(mai,"ranjankirtti2018@gmail.com");
            sm.setSubject("UserId For Online Transaction");
            sm.setText("Your UserId is : "+uidd);
            ms.send(sm);

            c.setUserId(uidd);
            cr.save(c);
            m.addAttribute("cust",new Customers());
            m.addAttribute("datasubm","Your Data is Submitted,Check Your Email for UserId ");
            return "index";
        }
    }
    @GetMapping("accdetails")
    String accDetails(Model m,HttpSession hs)
    {
        String tempid= (String) hs.getAttribute("sessionId");
//        System.out.println(tempid);
        Customers one = cr.getOne(tempid);
        m.addAttribute("accdata",one);
     return "accdetail";
    }
    @GetMapping("accupd")
    String accUpdate(Model m)
    {
        m.addAttribute("upddata",new Customers());
        return "accupdate";
    }
    @PostMapping("subaccupdate")
    String subAccUpdate(@ModelAttribute("upddata") Customers c,PrintWriter pw,HttpSession hp,Model m)
    {
        String id=(String) hp.getAttribute("sessionId");
        Customers one = cr.getOne(id);
        one.setUserId(id);
        one.setFirstName(c.getFirstName());
        one.setLastName(c.getLastName());
        one.setEmail(c.getEmail());
        one.setPhNo(c.getPhNo());
        one.setPassword(c.getPassword());
        one.setAdrs(c.getAdrs());
        cr.save(one);

        m.addAttribute("accdata",one);
        m.addAttribute("updatesuc","Your Account is Updated Now, Please Check Your Account");
        return "cushome";
    }
    @GetMapping("toamo")
    String totAmount(HttpSession hp,Model m)
    {
        String id=(String) hp.getAttribute("sessionId");
        Double amo=cr.getOne(id).getCash();
        m.addAttribute("totalamo",amo);
        return "totamount";
    }
    @GetMapping("tran")
    String Transaction(HttpSession hp)
    {

        return "transaction";
    }
    @PostMapping("subtransaction")
    String subTransaction(@RequestParam("tuid") String tuid,@RequestParam("tamo") Double tamo,Model m,HttpSession hp)
    {
        if(cr.existsById(tuid))
        {
            String id=(String) hp.getAttribute("sessionId");
            Double ownfee=cr.getOne(id).getCash();
            if (ownfee>=tamo) {
                if (cr.updateCus(tamo, id) != 0 && cr.updateTcus(tamo, tuid) != 0) {
                    Customers one = cr.getOne(id);
                    m.addAttribute("accdata",one);
                    m.addAttribute("updatesucc", "Transaction Completed..........\n Check Balance");
                    return "cushome";
                }
                else{
                    m.addAttribute("donttran","Transaction Incompleted.");
                    return "transaction";
                }
            }
            else {
                m.addAttribute("dontcash","!!! INSUFFICIENT BALANCE  !!! try again...");
                return "transaction";
            }

        }
        else{
            m.addAttribute("dontid","Entered UserId Doesn't Exist");
            return "transaction";
        }

    }

    @GetMapping("delacc")
    String delAccount(HttpSession hp,Model m)
    {
        m.addAttribute("cust",new Customers());
        String id=(String) hp.getAttribute("sessionId");
        Double ownbal=cr.getOne(id).getCash();
        if(ownbal>0.0)
        {
            m.addAttribute("accdontdelete","There is some balance having in account plz Transfer Before Deleting Your Account.");
            Customers one = cr.getOne(id);
            m.addAttribute("accdata",one);
            return "cushome";
        }
        else {
            Customers cu=cr.getOne(id);
            cr.delete(cu);
            m.addAttribute("accdelete","Your Account Deleted..Thanks for using..");
            return "index";
        }
    }
    @GetMapping("log")
    String  logout(HttpSession hs)
    {
        hs.setAttribute("sessionId",null);
        return "redirect:/";
    }
    @ResponseBody
    @GetMapping("demo")
    byte[] dem(@RequestParam("name") String name)throws Exception
    {
        File f=new File("img/"+name);
        return Files.readAllBytes(f.toPath());
    }
    @GetMapping("forgot")
    String forgetPass()
    {
        return "forgotpass";
    }
    @PostMapping("subforgot")
    String subForgot(@RequestParam("uid") String name,HttpSession hs,Model m)
    {
        if(cr.existsById(name))
        {
            String umail=cr.getOne(name).getEmail();

            Random rr=new Random();
            String ranval= String.valueOf(rr.nextInt(9999));
            hs.setAttribute("rec",ranval);
            hs.setAttribute("uid",name);
            SimpleMailMessage sm=new SimpleMailMessage();
            sm.setTo(umail,"ranjankirtti2018@gmail.com");
            sm.setSubject("Recovery Mail");
            sm.setText("Your Recovery Code is : "+ranval);
            ms.send(sm);
           // pw.println("Mail sended..");
            return "recovery";
        }
        else{
            m.addAttribute("idnot","Provided Id Doesn't Exist");
            return "forgotpass";
        }
    }
    @PostMapping("subrec")
    String subRec(@RequestParam("reckey") String key,HttpSession hs,Model m)
    {

        String temp1=(String) hs.getAttribute("rec");
       // System.out.println(key+"\t"+temp1);
        if(key.equals(temp1))
        {
            hs.setAttribute("rec",null);
            return "newpass";
        }
        else
        {
            m.addAttribute("keynot","Wrong Recovery Key , try again..");
            return "recovery";
        }
    }
    @PostMapping("subnewpass")
    String setPass(@RequestParam("pass1") String p1,@RequestParam("pass2") String p2,HttpSession hs,Model m)
    {
        if(p1.equals(p2))
        {
          String uid1=(String)hs.getAttribute("uid");
          hs.setAttribute("uid",null);
          //Customers cu=cr.getOne(uid1);
          if(cr.updatePass(uid1,p1)==1)
          {
              m.addAttribute("cust",new Customers());
              m.addAttribute("psuc","Your Password Changed Successfully, please login with new password");
             return "index";
          }
          else {
              m.addAttribute("upnot","Update Failed ! Try Again..");
              return "newpass";
          }
        }
        else {
            m.addAttribute("pnot","Both Password Does Mot Match");
            return "newpass";
        }
    }
}
