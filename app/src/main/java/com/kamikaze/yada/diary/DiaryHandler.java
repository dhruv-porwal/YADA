package com.kamikaze.yada.diary;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kamikaze.yada.R;
import com.kamikaze.yada.model.Notes;
import com.kamikaze.yada.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiaryHandler {
    User currentUser;
    Context context;

    public DiaryHandler(Context context) {
        this.context=context;
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String ok="";
        if(firebaseUser.getPhotoUrl()!=null) ok=firebaseUser.getPhotoUrl().toString();
        if(currentUser==null) currentUser=new User(firebaseUser.getUid(), firebaseUser.getDisplayName(),ok,new ArrayList<>(),"");
    }

    public void loadData(RecyclerView recyclerView)
    {
        ProgressDialog progressDialog= new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Loading");
        progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.empty_list_background);
        progressDialog.show();
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                progressDialog.cancel();
                if(task.isSuccessful())
                {
                    DocumentSnapshot documentSnapshot= task.getResult();
                    ArrayList<Diary> diaries= convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                    currentUser.setDiaries(diaries);
                    if(recyclerView.getAdapter()==null) recyclerView.setAdapter(new DiaryListRecyclerViewAdapter(context,diaries,recyclerView));
                    else recyclerView.swapAdapter(new DiaryListRecyclerViewAdapter(context,diaries,recyclerView),false);
                }
                else
                {
                    Toast.makeText(context, "You failed, failure", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void loadData()
    {
        FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        db.collection("users").document(firebaseUser.getUid()).get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot= task.getResult();
                ArrayList<Diary> diaries= convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                currentUser.setDiaries(diaries);
            }
            else
            {
                Toast.makeText(context, "You failed, failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deleteDiary(int position,RecyclerView recyclerView)
    {
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection("users").document(currentUser.getUid());
        documentReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot=task.getResult();
                ArrayList<Diary> diaries= convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                diaries.remove(position);
                DiaryListRecyclerViewAdapter adapter=new DiaryListRecyclerViewAdapter(context,diaries,recyclerView);
                recyclerView.swapAdapter(adapter,false);
                currentUser.setDiaries(diaries);
                Log.d("Size", String.valueOf(currentUser.getDiaries().size()));
                documentReference.update("diaries",diaries).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful())
                    {
                        Toast.makeText(context, "Diary deleted successfully", Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });
    }
    public static  int a;
    public ArrayList<Diary> getDiaries()
    {
        if(currentUser !=null)
        {
            loadData();
            return currentUser.getDiaries();
        }
        return new ArrayList<>();
    }

    public Diary getDiary(int position)//gib pos get diary
    {
        if(currentUser!=null && position<currentUser.getDiaries().size()) return getDiaries().get(position);
        return null;
    }

    public void addDiary(Diary diary, RecyclerView recyclerView) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("users").document(currentUser.getUid());
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    ArrayList<Diary> diaries = convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                    diaries.add(diary);
                    currentUser.setDiaries(diaries);
                    DiaryListRecyclerViewAdapter adapter = new DiaryListRecyclerViewAdapter(context, diaries, recyclerView);
                    recyclerView.swapAdapter(adapter, false);
                    Log.d("Size", String.valueOf(currentUser.getDiaries().size()));
                    documentReference.update("diaries", diaries).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(context, "New diary created successfully", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    public void updateDiary(int position,String bgImageUrl,RecyclerView recyclerView)
    {
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection("users").document(currentUser.getUid());
        documentReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot=task.getResult();
                ArrayList<Diary> diaries= convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                Diary item= diaries.get(position);
                item.setBgImageUrl(bgImageUrl);
                diaries.set(position,item);
                currentUser.setDiaries(diaries);
                DiaryListRecyclerViewAdapter adapter=new DiaryListRecyclerViewAdapter(context,diaries,recyclerView);
                recyclerView.swapAdapter(adapter,false);
                Log.d("Size", String.valueOf(currentUser.getDiaries().size()));
                documentReference.update("diaries",diaries).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful())
                    {
                        Log.d("Updation","Diary bgImageUrl updated successfully");
                    }
                });

            }
        });
    }


    public void updateDiary(int position,Notes note,int color)
    {
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection("users").document(currentUser.getUid());
        documentReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot=task.getResult();
                ArrayList<Diary> diaries= convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                Diary item= diaries.get(position);
                if(note==null) Log.d("note","null");
                else Log.d("note",note.getTextnote());
                item.setNote(note);
                if(color>0) item.setColor(color);
                diaries.set(position,item);
                currentUser.setDiaries(diaries);
                Log.d("Size", String.valueOf(currentUser.getDiaries().size()));
                documentReference.update("diaries",diaries).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful())
                    {   Log.d("xxyznote",note+"");
                        Log.d("Updation","Diary note updated successfully");
                    }
                });

            }
        });
    }

    public void updateDiary(int position,String imageUrl)
    {
        FirebaseFirestore db= FirebaseFirestore.getInstance();
        DocumentReference documentReference=db.collection("users").document(currentUser.getUid());
        documentReference.get().addOnCompleteListener(task -> {
            if(task.isSuccessful())
            {
                DocumentSnapshot documentSnapshot=task.getResult();
                ArrayList<Diary> diaries= convertToDiary((List<HashMap<String, Object>>) documentSnapshot.get("diaries"));
                Diary item= diaries.get(position);
                List<String> images=item.getImages();
                images.add(imageUrl);
                item.setImages(images);
                diaries.set(position,item);
                currentUser.setDiaries(diaries);
                Log.d("Size", String.valueOf(currentUser.getDiaries().size()));
                documentReference.update("diaries",diaries).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful())
                    {
                        Log.d("Updation","Diary images updated successfully");
                    }
                });

            }
        });
    }


    public ArrayList<Diary> convertToDiary(List<HashMap<String,Object>> diaryContent)
    {
        ArrayList<Diary> diaries=new ArrayList<>();
        if(diaryContent==null) return diaries;
        for(int i=0;i<diaryContent.size();i++)
        {
            HashMap<String,String> noteContent= (HashMap<String, String>) diaryContent.get(i).get("note");
            Notes note=null;
            String dtitle=null;
            String ddescription=null;
            String dlocation=null;
            String dbgImageUrl=null;
            int color=-1;
            List<String> dimages=new ArrayList<>();
            if(diaryContent.get(i).get("title")!=null) dtitle=diaryContent.get(i).get("title").toString();
            if(diaryContent.get(i).get("description")!=null) ddescription=diaryContent.get(i).get("description").toString();
            if(diaryContent.get(i).get("location")!=null) dlocation=diaryContent.get(i).get("location").toString();
            if(diaryContent.get(i).get("bgImageUrl")!=null) dbgImageUrl=diaryContent.get(i).get("bgImageUrl").toString();
            if(diaryContent.get(i).get("images")!=null) dimages=(List<String>) diaryContent.get(i).get("images");

            if(diaryContent.get(i).get("color")!=null) color= Integer.parseInt(diaryContent.get(i).get("color").toString()) ;

            if(noteContent!=null) note=new Notes(noteContent.get("topic"),noteContent.get("description"),noteContent.get("location"),noteContent.get("textnote"));

            diaries.add(new Diary(dtitle,ddescription,dlocation,dbgImageUrl,note,dimages,color));         }
        return diaries;
    }
}
