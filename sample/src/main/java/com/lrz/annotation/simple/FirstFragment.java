package com.lrz.annotation.simple;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.lrz.annotation.simple.data.TextConfig;
import com.lrz.annotation.simple.databinding.FragmentFirstBinding;
import com.lrz.multi.MultiData;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    TextConfig config;

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });

        binding.b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserInfo info = new UserInfo();
                info.name = "保存";
                info.b = false;
                info.i = 1;
                MultiData.DATA.get(DataSimple.class).getMap().put("data", info);
                MultiData.DATA.get(DataSimple.class).getList().add(info);
                MultiData.DATA.get(DataSimple.class).getSet().add(info);
                MultiData.DATA.get(DataSimple.class).getMapI().put("1", 1);
            }
        });

        binding.b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiData.DATA.get(DataSimple.class).getMap().get("data").i = 3;
                MultiData.DATA.get(DataSimple.class).getMap().get("data").b = true;
            }
        });
        binding.b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Map<String, UserInfo> map = MultiDataUtil.getHash("data_simple", "map", new HashMap<String, UserInfo>());
                System.out.println("------读取" + MultiData.DATA.get(DataSimple.class).getMap().get("data").name);
                System.out.println("------读取2" + MultiData.DATA.get(DataSimple.class).getList().get(0).name);
                System.out.println("------读取3" + MultiData.DATA.get(DataSimple.class).getSet().iterator().next().name);
                System.out.println("------读取3" + MultiData.DATA.get(DataSimple.class).getMapI().get("1"));
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}