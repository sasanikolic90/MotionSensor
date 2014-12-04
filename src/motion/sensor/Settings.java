package motion.sensor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends Activity {

	Button save, close;
	static EditText email,percentage,treshold,delay;
	SharedPreferences sharedPreferences;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		sharedPreferences = PIMActivity.con.getSharedPreferences("MOTION", MODE_WORLD_READABLE);

		save = (Button) findViewById(R.id.button1);
		close = (Button) findViewById(R.id.button2);
		email = (EditText) findViewById(R.id.editText1);
		percentage = (EditText) findViewById(R.id.editText2);
		treshold = (EditText) findViewById(R.id.editText3);
		delay = (EditText) findViewById(R.id.editText4);

		email.setText(sharedPreferences.getString("email", "empty"));
		percentage.setText(sharedPreferences.getFloat("percentage", 3)+"");
		treshold.setText(sharedPreferences.getFloat("treshold", 10)+"");
		delay.setText(sharedPreferences.getInt("delay", 10)+"");
		
		email.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
			if (email.getText().toString().equals("empty"))
				email.setText("");
			}
		});
		
		close.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();	
			}
		});

		save.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				SharedPreferences.Editor editor = sharedPreferences.edit();
				editor.putString("email", email.getText().toString());
				editor.putFloat("percentage", Float.parseFloat(percentage.getText().toString()));
				editor.putFloat("treshold", Float.parseFloat(treshold.getText().toString()));
				editor.putInt("delay", Integer.parseInt(delay.getText().toString()));
				editor.commit();
				PIMActivity.mail = email.getText().toString();
				PIMActivity.percentage = Float.parseFloat(percentage.getText().toString())/100;
				PIMActivity.treshold = Float.parseFloat(treshold.getText().toString())*255/100;
				PIMActivity.delay = Integer.parseInt(delay.getText().toString());
				if (PIMActivity.percentage > 0 && PIMActivity.treshold > 0 && PIMActivity.mail.contains("@"))
					finish();
				else 
					Toast.makeText(PIMActivity.con, "Set all parameters.", Toast.LENGTH_LONG).show();
			}
		});

	}

}
