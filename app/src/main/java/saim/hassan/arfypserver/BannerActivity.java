package saim.hassan.arfypserver;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import saim.hassan.arfypserver.Common.Common;
import saim.hassan.arfypserver.Model.Banner;
import saim.hassan.arfypserver.Model.Product;
import saim.hassan.arfypserver.ViewHolder.BannerViewHolder;

public class BannerActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab;
    RelativeLayout rootLayout;

    //Firebase
    FirebaseDatabase db;
    DatabaseReference banners;
    FirebaseStorage storage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapter;

    //Add new Banner
    MaterialEditText edtName, edtProductId;
    Button btnUpload, btnSelect;

    Banner newBanner;
    Uri filepath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_banner);

        //Init Firebase
        db = FirebaseDatabase.getInstance();
       // banners = db.getReference("POS").child(Common.posSelected).child("detail").child("Banner");
        banners = db.getReference("Banner");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_banner);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        rootLayout = (RelativeLayout) findViewById(R.id.root_layout);

        fab = (FloatingActionButton) findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddBanner();
            }
        });
        loadListBanner();
    }

    private void loadListBanner() {
        FirebaseRecyclerOptions<Banner> allBanner = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(banners, Banner.class).build();
        adapter = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(allBanner) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder holder, int position, @NonNull Banner model) {
                holder.textbanner.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(holder.imagebanner);
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.banner_layout, viewGroup, false);
                return new BannerViewHolder(itemView);
            }
        };
        adapter.startListening();
        // set Adapter
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private void showAddBanner() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Add new Banner");
        alertDialog.setMessage("Please fill full information");
        LayoutInflater inflater = this.getLayoutInflater();
        View v = inflater.inflate(R.layout.add_new_banner, null);

        edtProductId = v.findViewById(R.id.edtProductId);
        edtName = v.findViewById(R.id.edtProductName);

        btnSelect = v.findViewById(R.id.btnselect);
        btnUpload = v.findViewById(R.id.btnupload);

        //Set Event for Select Picture From Phone Gallery
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        //Set Event for Upload Picture from Gallery
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(v);
        alertDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //Set Button For Dialog
        alertDialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (newBanner != null)
                    banners.push()
                            .setValue(newBanner);

                loadListBanner();

            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                newBanner = null;
                loadListBanner();

            }
        });
        alertDialog.show();
    }

    private void uploadImage() {

        if (filepath != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set Value for new Category if image upload and we can get Download Link
                                    newBanner = new Banner();
                                    newBanner.setName(edtName.getText().toString());
                                    newBanner.setId(edtProductId.getText().toString());
                                    newBanner.setImage(uri.toString());
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() /
                                    taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded" + progress + "%");
                        }
                    });
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "SELECT PICTURE"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filepath = data.getData();
            btnSelect.setText("Image Selected !");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateBannerDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteBanner(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteBanner(String key) {
    }

    private void showUpdateBannerDialog(final String key, final Banner item) {

        // just copy code showAddProductDiallog Method
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(BannerActivity.this);
        alertDialog.setTitle("Edit Banner");
        alertDialog.setMessage("Please fill full informations");
        LayoutInflater inflater = this.getLayoutInflater();
        View edtBanner = inflater.inflate(R.layout.add_new_banner, null);

        edtName = edtBanner.findViewById(R.id.edtProductName);
        edtProductId = edtBanner.findViewById(R.id.edtProductId);
        //Set Default Value for View
        edtName.setText(item.getName());
        edtProductId.setText(item.getId());
        btnSelect = edtBanner.findViewById(R.id.btnselect);
        btnUpload = edtBanner.findViewById(R.id.btnupload);

        //Event For Button
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(edtBanner);
        alertDialog.setIcon(R.drawable.ic_laptop_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                 item.setName(edtName.getText().toString());
                 item.setId(edtProductId.getText().toString());

                 //Make Update
                Map<String,Object> update = new HashMap<>();
                update.put("id",item.getId());
                update.put("name",item.getName());
                update.put("image",item.getImage());

                banners.child(key)
                        .updateChildren(update)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Snackbar.make(rootLayout,"Updated",Snackbar.LENGTH_SHORT).show();
                                loadListBanner();
                            }
                        });

                Snackbar.make(rootLayout, "Product" + item.getName() + "was edited"
                        , Snackbar.LENGTH_SHORT
                ).show();
                loadListBanner();


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                loadListBanner();

            }
        });
        alertDialog.show();

    }

    private void changeImage(final Banner item) {
        if (filepath != null) {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/" + imageName);
            imageFolder.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set Value for new Category if image upload and we can get Download Link
                                    item.setImage(uri.toString());
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(BannerActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() /
                                    taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded" + progress + "%");
                        }
                    });
        }
    }
}
