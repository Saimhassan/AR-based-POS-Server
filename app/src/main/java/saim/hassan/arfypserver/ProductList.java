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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import saim.hassan.arfypserver.Common.Common;
import saim.hassan.arfypserver.Interface.ItemClickListener;
import saim.hassan.arfypserver.Model.Category;
import saim.hassan.arfypserver.Model.Product;
import saim.hassan.arfypserver.ViewHolder.ProductViewHolder;

public class ProductList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FloatingActionButton fab;
    RelativeLayout rootLayout;

    //Firebase
    FirebaseDatabase db;
    DatabaseReference productList;
    FirebaseStorage storage;
    StorageReference storageReference;

    String categoryId = "";
    FirebaseRecyclerAdapter<Product, ProductViewHolder> adapter;


    //Add new Product
    MaterialEditText edtName,edtprice,edtdiscount,edtdesc;
    Button btnselectt,btnuplload;

    Product newProduct;
    Uri saveuri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        //Firebase
        db = FirebaseDatabase.getInstance();
     //   productList = db.getReference("POS").child(Common.posSelected).child("detail").child("Products");
        productList = db.getReference("Products");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        //Init
        recyclerView = (RecyclerView)findViewById(R.id.recycler_productser);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        rootLayout = (RelativeLayout)findViewById(R.id.root_layout);

        fab = (FloatingActionButton)findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddProductDialog();
            }
        });
        if (getIntent()!=null)
            categoryId = getIntent().getStringExtra("CategoryId");
        if (!categoryId.isEmpty())
        {
             loadListProducts(categoryId);
        }

    }

    private void showAddProductDialog() {
        //Copy Code from the Home Activity

        AlertDialog.Builder alertDialog  = new AlertDialog.Builder(ProductList.this);
        alertDialog.setTitle("Add new Product");
        alertDialog.setMessage("Please fill full informations");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_product_layout = inflater.inflate(R.layout.add_new_product_layout,null);

        edtName = add_product_layout.findViewById(R.id.edtName);
        edtdiscount = add_product_layout.findViewById(R.id.edtdiscount);
        edtprice = add_product_layout.findViewById(R.id.edtprice);
        edtdesc = add_product_layout.findViewById(R.id.edtdesc);
        btnselectt = add_product_layout.findViewById(R.id.btnselect);
        btnuplload = add_product_layout.findViewById(R.id.btnupload);

        //Event For Button
        btnselectt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnuplload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setView(add_product_layout);
        alertDialog.setIcon(R.drawable.ic_add_shopping_cart_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                //Here Just Create Product
                if (newProduct != null )
                {
                    productList.push().setValue(newProduct);
                    Snackbar.make(rootLayout,"New Product"+newProduct.getName()+ "was added"
                            ,Snackbar.LENGTH_SHORT
                    ).show();
                }

            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });
        alertDialog.show();
    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"SELECT PICTURE"), Common.PICK_IMAGE_REQUEST);

    }

    private void uploadImage(){
        if (saveuri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    //set Value for new Category if image upload and we can get Download Link
                                    newProduct = new Product();
                                    newProduct.setName(edtName.getText().toString());
                                    newProduct.setPrice(edtprice.getText().toString());
                                    newProduct.setDescription(edtdesc.getText().toString());
                                    newProduct.setDiscount(edtdiscount.getText().toString());
                                    newProduct.setMenuId(categoryId);
                                    newProduct.setImage(uri.toString());
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/
                                    taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded"+progress+"%");
                        }
                    });
        }
    }

    private void loadListProducts(final String categoryId) {

        Query listProductByCategoryId = productList.orderByChild("menuId").equalTo(categoryId);

        FirebaseRecyclerOptions<Product> options = new FirebaseRecyclerOptions.Builder<Product>()
                .setQuery(listProductByCategoryId,Product.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ProductViewHolder holder, int position, @NonNull Product model) {
                holder.textproduct.setText(model.getName());
                Picasso.with(getBaseContext())
                        .load(model.getImage())
                        .into(holder.imageproduct);
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        //Code Late
                    }
                });
            }

            @NonNull
            @Override
            public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View itemView = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.product_item,viewGroup,false);
                return new ProductViewHolder(itemView);
            }
        };
        adapter.startListening();

        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData()!= null)
        {
            saveuri = data.getData();
            btnselectt.setText("Image Selected !");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.UPDATE))
        {
                  showUpdateProductDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if (item.getTitle().equals(Common.DELETE))
        {
                    deleteProduct(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void deleteProduct(String key) {
        productList.child(key).removeValue();
    }

    private void showUpdateProductDialog(final String key, final Product item) {
        // just copy code showAddProductDiallog Method
        AlertDialog.Builder alertDialog  = new AlertDialog.Builder(ProductList.this);
        alertDialog.setTitle("Edit Product");
        alertDialog.setMessage("Please fill full informations");
        LayoutInflater inflater = this.getLayoutInflater();
        View add_product_layout = inflater.inflate(R.layout.add_new_product_layout,null);

        edtName = add_product_layout.findViewById(R.id.edtName);
        edtdiscount = add_product_layout.findViewById(R.id.edtdiscount);
        edtprice = add_product_layout.findViewById(R.id.edtprice);
        edtdesc = add_product_layout.findViewById(R.id.edtdesc);

        //Set Default Value for View
        edtName.setText(item.getName());
        edtdesc.setText(item.getDescription());
        edtprice.setText(item.getPrice());
        edtdiscount.setText(item.getDiscount());

        btnselectt = add_product_layout.findViewById(R.id.btnselect);
        btnuplload = add_product_layout.findViewById(R.id.btnupload);

        //Event For Button
        btnselectt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnuplload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        alertDialog.setView(add_product_layout);
        alertDialog.setIcon(R.drawable.ic_add_shopping_cart_black_24dp);

        //Set Button
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();


                    //UPDATE INFORMATION
                    item.setName(edtName.getText().toString());
                    item.setDescription(edtdesc.getText().toString());
                    item.setPrice(edtprice.getText().toString());
                    item.setDiscount(edtdiscount.getText().toString());


                    productList.child(key).setValue(item);
                    Snackbar.make(rootLayout,"Product"+item.getName()+ "was edited"
                            ,Snackbar.LENGTH_SHORT
                    ).show();


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

            }
        });
        alertDialog.show();

    }

    private void changeImage(final Product item) {

        if (saveuri != null)
        {
            final ProgressDialog mDialog = new ProgressDialog(this);
            mDialog.setMessage("Uploading");
            mDialog.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("images/"+imageName);
            imageFolder.putFile(saveuri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            mDialog.dismiss();
                            Toast.makeText(ProductList.this, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ProductList.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/
                                    taskSnapshot.getTotalByteCount());
                            mDialog.setMessage("Uploaded"+progress+"%");
                        }
                    });
        }


    }

}
